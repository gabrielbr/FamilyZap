// Package oielink is Oiê's link to the parent's WhatsApp: whatsmeow, wrapped for gomobile.
//
// The app (Kotlin) drives it through Link and hears back through Listener as (kind, json) events.
// Everything that decides what the kid sees stays in Kotlin (Inbox.decide): this package only
// translates. The rules L1–L9 in docs/whatsapp-link.md apply to all of it.
//
// Status: not wired into the app yet; the app runs on SampleLink until the message rule is tested
// with real chats (STATUS.md).
//
// whatsmeow is MPL-2.0: we use it unmodified and say where its source is (docs/whatsapp-link.md).
package oielink

import (
	"bytes"
	"context"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"image"
	"image/jpeg"
	"io"
	"net/http"
	"os"
	"runtime/debug"
	"sort"
	"strings"
	"sync"
	"time"

	"go.mau.fi/whatsmeow"
	"go.mau.fi/whatsmeow/appstate"
	"go.mau.fi/whatsmeow/proto/waCommon"
	"go.mau.fi/whatsmeow/proto/waE2E"
	"go.mau.fi/whatsmeow/store"
	"go.mau.fi/whatsmeow/store/sqlstore"
	"go.mau.fi/whatsmeow/types"
	"go.mau.fi/whatsmeow/types/events"
	waLog "go.mau.fi/whatsmeow/util/log"
	"google.golang.org/protobuf/proto"
	_ "modernc.org/sqlite"
)

// CrashSuffix names the file, next to the session store, where Go writes a fatal error.
const CrashSuffix = ".crash"

// MaxReconnects is how many automatic reconnects whatsmeow tries before it stops and waits for
// the app (rule L5). Its own wait grows 2 s per try.
const MaxReconnects = 8

// ContactsGrace is how long after connecting the link waits for the phone's contacts.
const ContactsGrace = 8 * time.Second

// MinSendGap backs up the app's SendPace (rule L3): even a bug in the app can't send faster.
const MinSendGap = 2 * time.Second

var (
	ErrReadOnly  = errors.New("read-only: sending is off")
	ErrTooFast   = errors.New("too fast: wait before sending again")
	ErrNotLinked = errors.New("not linked")
	ErrStopped   = errors.New("stopped after a ban or logout")
)

// Listener receives events as (kind, json). Kotlin implements it. Kinds:
//
//	qr {code, timeoutSeconds} · paired {} · pairError {error} · connected {} · disconnected {}
//	message (Incoming) · loggedOut {reason} · temporaryBan {code, expiresInSeconds} · replaced {}
type Listener interface {
	OnEvent(kind string, payload string)
}

// Link is one tablet's link to one WhatsApp account.
type Link struct {
	c        *whatsmeow.Client
	l        Listener
	mu       sync.Mutex
	readOnly bool
	stopped  bool
	lastSend time.Time
	// Media the app may still download, by message id. Bounded: the oldest go first.
	media      map[string]whatsmeow.DownloadableMessage
	mediaOrder []string
	// One "contactsChanged" for a burst of contact updates.
	contactsTimer *time.Timer
	// parentLine is the chat where the kid writes to the parent who owns this WhatsApp (v0.30): a
	// one-person group, or the parent's own chat when WhatsApp refused the group.
	parentLine string
	// statusMu holds one status post at a time, so the chosen audience can't leak into another send (v0.37).
	statusMu sync.Mutex
	// groups keeps the last good list of the parent's groups (v0.42): asking WhatsApp on every
	// contacts reload got refused at times, and the groups vanished from the parent's list.
	groups groupCache
}

// GroupsRefresh is how long the list of groups is reused before WhatsApp is asked again (v0.42),
// unless WhatsApp says the groups changed (joined, left, renamed).
const GroupsRefresh = 10 * time.Minute

// groupCache is the last good list of groups. A failed ask keeps the last good list.
type groupCache struct {
	mu    sync.Mutex
	list  []contactEntry
	at    time.Time
	stale bool
}

// get returns the groups, asking [fetch] only when there's no list yet, it's older than
// GroupsRefresh, or it was marked stale. On an error it returns the last good list and the error.
func (g *groupCache) get(now time.Time, fetch func() ([]contactEntry, error)) ([]contactEntry, error) {
	g.mu.Lock()
	defer g.mu.Unlock()
	if g.list != nil && !g.stale && now.Sub(g.at) < GroupsRefresh {
		return g.list, nil
	}
	fresh, err := fetch()
	if err != nil {
		return g.list, err
	}
	if fresh == nil {
		fresh = []contactEntry{}
	}
	g.list, g.at, g.stale = fresh, now, false
	return fresh, nil
}

// markStale makes the next get ask WhatsApp again.
func (g *groupCache) markStale() { g.mu.Lock(); g.stale = true; g.mu.Unlock() }

// MaxPendingMedia is how many received photos, voice notes and stickers stay downloadable.
const MaxPendingMedia = 200

func init() {
	// L1: the parent sees "Oiê" in WhatsApp's Linked devices, so it's never a mystery device.
	store.SetOSInfo("Oiê", [3]uint32{0, 5, 0})
	// L6: pull as little history as WhatsApp allows. Oiê shows nothing from before the link.
	one := uint32(1)
	store.DeviceProps.HistorySyncConfig.FullSyncDaysLimit = &one
	store.DeviceProps.HistorySyncConfig.RecentSyncDaysLimit = &one
}

// Open opens (or creates) the session store at dbPath, in the app's private storage (rule L8).
func Open(dbPath string, l Listener) (*Link, error) {
	ctx := context.Background()
	// v0.15: a Go fatal error (or a panic no guard caught) ends the whole app without an Android
	// crash. Go writes what happened to this file too, and the app adds it to the problem report
	// the next time it opens (docs/flows/problem-report.md).
	if f, err := os.OpenFile(dbPath+CrashSuffix, os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0o600); err == nil {
		_ = debug.SetCrashOutput(f, debug.CrashOptions{})
		f.Close()
	}
	container, err := sqlstore.New(ctx, "sqlite", storeDSN(dbPath), &reportLog{l: l, module: "store"})
	if err != nil {
		return nil, err
	}
	dev, err := container.GetFirstDevice(ctx)
	if err != nil {
		return nil, err
	}
	c := whatsmeow.NewClient(dev, &reportLog{l: l, module: "client"})
	// L5: bounded reconnects; after that the app reconnects when the network or the app comes back.
	c.AutoReconnectHook = func(error) bool { return c.AutoReconnectErrors < MaxReconnects }
	k := &Link{c: c, l: l}
	c.AddEventHandler(k.handle)
	return k, nil
}

// IsConnected says whether the connection to WhatsApp is up and logged in right now (v0.20).
func (k *Link) IsConnected() bool { return k.c.IsConnected() && k.c.IsLoggedIn() }

// alreadyUp is whatsmeow's "the websocket is already connected", which Connect treats as success.
func alreadyUp(err error) bool { return errors.Is(err, whatsmeow.ErrAlreadyConnected) }

// storeDSN opens the session database. v0.23: temp_store(2) keeps SQLite's temporary tables in
// memory. Android has none of the temp folders SQLite looks for, so a big write (WhatsApp's
// redacted phone numbers from joined groups) failed with "disk I/O error (6410)", which is
// SQLITE_IOERR_GETTEMPPATH (the owner's report, issue #34).
func storeDSN(dbPath string) string {
	return "file:" + dbPath + "?_pragma=foreign_keys(1)&_pragma=journal_mode(WAL)&_pragma=temp_store(2)"
}

// Paired says whether the tablet is already linked to a WhatsApp account.
func (k *Link) Paired() bool { return k.c.Store.ID != nil }

// SetReadOnly turns sending off (true) or on. Read-only is for testing the message rule on a real
// account: the link only listens.
func (k *Link) SetReadOnly(on bool) { k.mu.Lock(); k.readOnly = on; k.mu.Unlock() }

// Connect connects. When the tablet isn't linked yet, it emits "qr" events for the parent to scan
// in WhatsApp › Linked devices, then "paired" or "pairError".
func (k *Link) Connect() (err error) {
	defer k.guard("Connect", &err)
	if k.isStopped() {
		return ErrStopped
	}
	ctx := context.Background()
	if k.Paired() {
		// v0.20: Oiê asks to connect every time it comes back to the screen. When the connection is
		// already up, whatsmeow says "already connected"; that's not a failure (the owner saw the
		// tablet say "disconnected" while WhatsApp showed it connected). Say "connected" instead.
		if err := k.c.Connect(); err != nil && !alreadyUp(err) {
			return err
		} else if err != nil && k.c.IsLoggedIn() {
			k.emit("connected", map[string]any{"already": true})
		}
		return nil
	}
	qr, err := k.c.GetQRChannel(ctx)
	if err != nil {
		return err
	}
	if err := k.c.Connect(); err != nil {
		return err
	}
	go func() {
		for item := range qr {
			switch item.Event {
			case "code":
				k.emit("qr", map[string]any{"code": item.Code, "timeoutSeconds": int(item.Timeout.Seconds())})
			case "success":
				k.emit("paired", map[string]any{})
			default:
				msg := item.Event
				if item.Error != nil {
					msg = item.Error.Error()
				}
				k.emit("pairError", map[string]any{"error": msg})
			}
		}
	}()
	return nil
}

// PairWithCode is the "Usar código" option: it returns the 8-character code the parent types in
// WhatsApp › Linked devices › Link with phone number. Call after Connect.
func (k *Link) PairWithCode(phone string) (code string, err error) {
	defer k.guard("PairWithCode", &err)
	return k.c.PairPhone(context.Background(), phone, true, whatsmeow.PairClientChrome, "Chrome (Linux)")
}

// Disconnect closes the connection; the link stays (the tablet can reconnect without a new QR).
func (k *Link) Disconnect() { k.c.Disconnect() }

// Unlink logs out and deletes the session from the tablet (rule L8).
func (k *Link) Unlink() (err error) {
	defer k.guard("Unlink", &err)
	ctx := context.Background()
	if k.Paired() {
		if err := k.c.Logout(ctx); err != nil {
			// Logged out on the phone already, or offline: delete the local session anyway.
			_ = k.c.Store.Delete(ctx)
		}
	}
	return nil
}

// Contacts returns the parent's WhatsApp contacts and groups as JSON, for the parent to choose
// from: [{"id","name","isGroup"}], sorted by name. It never goes to the kid's screens.
func (k *Link) Contacts() (list string, err error) {
	defer k.guard("Contacts", &err)
	if !k.Paired() {
		return "", ErrNotLinked
	}
	ctx := context.Background()
	var out []contactEntry
	all, err := k.c.Store.Contacts.GetAllContacts(ctx)
	if err != nil {
		return "", err
	}
	for jid, info := range all {
		if name := contactName(info); name != "" {
			out = append(out, contactEntry{jid.ToNonAD().String(), name, false, k.phoneOf(ctx, jid)})
		}
	}
	groups, gerr := k.groups.get(time.Now(), func() ([]contactEntry, error) {
		joined, err := k.c.GetJoinedGroups(ctx)
		if err != nil {
			return nil, err
		}
		list := make([]contactEntry, 0, len(joined))
		for _, g := range joined {
			list = append(list, contactEntry{g.JID.String(), g.GroupName.Name, true, ""})
		}
		return list, nil
	})
	if gerr != nil {
		// For the problem report: the reason only, never a group's name.
		k.emit("groupsError", map[string]any{"error": gerr.Error(), "kept": len(groups)})
	}
	out = append(out, groups...)
	return encodeContacts(out)
}

type contactEntry struct {
	ID      string `json:"id"`
	Name    string `json:"name"`
	IsGroup bool   `json:"isGroup"`
	// Phone is the number's digits with the country code, for the parent to check they picked the
	// right person (v0.19). Empty when WhatsApp only knows the contact by a private id (LID).
	Phone string `json:"phone,omitempty"`
}

// phoneOf is the contact's number: the user part of a phone JID, or the phone JID WhatsApp
// mapped to a private id (LID). Empty when there's none.
func (k *Link) phoneOf(ctx context.Context, jid types.JID) string {
	if p := phoneDigits(jid); p != "" {
		return p
	}
	if jid.Server == types.HiddenUserServer && k.c.Store.LIDs != nil {
		if pn, err := k.c.Store.LIDs.GetPNForLID(ctx, jid); err == nil {
			return phoneDigits(pn)
		}
	}
	return ""
}

// phoneDigits is the user part of a phone-number JID ("5511987650001@s.whatsapp.net"), else "".
func phoneDigits(jid types.JID) string {
	if jid.Server != types.DefaultUserServer {
		return ""
	}
	return DigitsOnly(jid.User)
}

// encodeContacts sorts by name and encodes. An empty list is "[]", never "null": right after
// linking there are no contacts yet, and "null" crashed the app mid-link, which made WhatsApp drop
// the tablet (v0.16).
func encodeContacts(out []contactEntry) (string, error) {
	if out == nil {
		out = []contactEntry{}
	}
	sort.Slice(out, func(i, j int) bool { return out[i].Name < out[j].Name })
	b, err := json.Marshal(out)
	return string(b), err
}

// SendText sends a text to an allowed chat (the app checks the list, rule L2) and returns the
// message id. quotedID replies to a message; empty for none. A failure is reported, never retried
// here (rule L4): the app retries once with the same id via SendTextWithID.
func (k *Link) SendText(chat, text, quotedID string) (string, error) {
	return k.SendTextWithID(chat, text, quotedID, string(k.c.GenerateMessageID()))
}

// SendTextWithID sends with a given id, so a retry after a reconnect can't make a duplicate.
func (k *Link) SendTextWithID(chat, text, quotedID, id string) (sent string, err error) {
	defer k.guard("SendText", &err)
	to, err := k.target(chat)
	if err != nil {
		return "", err
	}
	if err := k.maySend(); err != nil {
		return "", err
	}
	msg := &waE2E.Message{Conversation: proto.String(text)}
	if quotedID != "" {
		msg = &waE2E.Message{ExtendedTextMessage: &waE2E.ExtendedTextMessage{
			Text:        proto.String(text),
			ContextInfo: &waE2E.ContextInfo{StanzaID: proto.String(quotedID)},
		}}
	}
	_, err = k.c.SendMessage(context.Background(), to, msg, whatsmeow.SendRequestExtra{ID: types.MessageID(id)})
	return id, err
}

func (k *Link) maySend() error {
	k.mu.Lock()
	defer k.mu.Unlock()
	switch {
	case k.stopped:
		return ErrStopped
	case k.readOnly:
		return ErrReadOnly
	case !k.lastSend.IsZero() && time.Since(k.lastSend) < MinSendGap:
		return ErrTooFast
	}
	k.lastSend = time.Now()
	return nil
}

func (k *Link) isStopped() bool { k.mu.Lock(); defer k.mu.Unlock(); return k.stopped }

func (k *Link) stop() { k.mu.Lock(); k.stopped = true; k.mu.Unlock() }

func (k *Link) handle(e interface{}) {
	switch v := e.(type) {
	case *events.Message:
		if in, ok := ToIncoming(v.Info, v.Message); ok {
			if own := k.c.Store.ID; own != nil {
				in.SelfChat = IsSelfChat(v.Info.Chat, *own, k.c.Store.LID)
			}
			if d := downloadable(v.Message); d != nil {
				k.remember(in.ID, d)
			}
			k.emit("message", in)
		}
	case *events.Connected:
		// L7: one presence so WhatsApp has the name, and it's "unavailable": the parent never
		// shows as online because of the tablet.
		_ = k.c.SendPresence(context.Background(), types.PresenceUnavailable)
		k.emit("connected", map[string]any{})
		// Contacts arrive from the phone a little after linking. If none have come after a few
		// seconds, ask for them once (only when not synced yet), then tell the app to reload.
		go func() {
			defer k.guard("contacts", nil)
			time.Sleep(ContactsGrace)
			ctx := context.Background()
			if all, err := k.c.Store.Contacts.GetAllContacts(ctx); err == nil && len(all) == 0 {
				if k.c.FetchAppState(ctx, appstate.WAPatchCriticalUnblockLow, true, true) == nil {
					k.emit("contactsSynced", map[string]any{})
				}
			} else if err == nil {
				k.emit("contactsSynced", map[string]any{}) // already here from before
			}
			k.contactsChanged()
		}()
	case *events.AppStateSyncComplete:
		// The contact list snapshot is in: the app stops saying "bringing your contacts" (v0.17).
		if v.Name == appstate.WAPatchCriticalUnblockLow {
			k.emit("contactsSynced", map[string]any{})
		}
		k.contactsChanged()
	case *events.Contact, *events.PushName:
		k.contactsChanged()
	case *events.JoinedGroup, *events.GroupInfo:
		// v0.42: a group joined, left or renamed: ask WhatsApp for the groups on the next reload.
		k.groups.markStale()
		k.contactsChanged()
	case *events.Receipt:
		// v0.33: "Entregue" and "Vovó viu" under the kid's message. Only the family's receipts for
		// messages; the parent's own devices ("read-self") and retries say nothing to the kid.
		if r := ReceiptOf(v); r != "" {
			ids := make([]string, len(v.MessageIDs))
			for i, id := range v.MessageIDs {
				ids[i] = string(id)
			}
			k.emit("receipt", map[string]any{"ids": ids, "chat": v.Chat.ToNonAD().String(), "state": r})
		}
	case *events.Disconnected:
		k.emit("disconnected", map[string]any{})
	case *events.KeepAliveTimeout:
		// v0.20: for the problem report. whatsmeow reconnects by itself after 3 minutes of these.
		k.emit("keepAliveTimeout", map[string]any{"errors": v.ErrorCount})
	case *events.KeepAliveRestored:
		k.emit("keepAliveRestored", map[string]any{})
	case *events.StreamReplaced:
		k.emit("replaced", map[string]any{})
	case *events.LoggedOut:
		k.stop() // L9
		k.emit("loggedOut", map[string]any{"reason": int(v.Reason)})
	case *events.TemporaryBan:
		k.stop() // L9: stop everything and tell the parent plainly.
		k.emit("temporaryBan", map[string]any{"code": int(v.Code), "expiresInSeconds": int64(v.Expire.Seconds())})
	}
	// History sync (events.HistorySync) is ignored on purpose (rule L6).
}

// guard turns a panic in this package or in whatsmeow into an error, so it can never close the app,
// and reports it with its stack for the problem report (v0.14).
func (k *Link) guard(where string, err *error) {
	if r := recover(); r != nil {
		stack := string(debug.Stack())
		if len(stack) > 3000 {
			stack = stack[:3000]
		}
		k.emit("log", map[string]any{"level": "panic", "msg": fmt.Sprintf("%s: %v\n%s", where, r, stack)})
		if err != nil {
			*err = fmt.Errorf("internal error in %s: %v", where, r)
		}
	}
}

// reportLog hands whatsmeow's warnings and errors to the app's problem report. Debug and info are dropped.
type reportLog struct {
	l      Listener
	module string
}

func (r *reportLog) send(level, format string, args ...any) {
	if r.l == nil {
		return
	}
	b, _ := json.Marshal(map[string]any{"level": level, "msg": r.module + ": " + fmt.Sprintf(format, args...)})
	r.l.OnEvent("log", string(b))
}
func (r *reportLog) Warnf(msg string, args ...any)  { r.send("warn", msg, args...) }
func (r *reportLog) Errorf(msg string, args ...any) { r.send("error", msg, args...) }
func (r *reportLog) Infof(string, ...any)           {}
func (r *reportLog) Debugf(string, ...any)          {}
func (r *reportLog) Sub(module string) waLog.Logger {
	return &reportLog{l: r.l, module: r.module + "/" + module}
}

// contactsChanged tells the app, once per burst (contacts arrive one by one), to reload Contacts().
func (k *Link) contactsChanged() {
	k.mu.Lock()
	defer k.mu.Unlock()
	if k.contactsTimer != nil {
		return
	}
	k.contactsTimer = time.AfterFunc(2*time.Second, func() {
		k.mu.Lock()
		k.contactsTimer = nil
		k.mu.Unlock()
		k.emit("contactsChanged", map[string]any{})
	})
}

// LookUp finds a phone number on WhatsApp, for the parent to add someone missing from the list.
// phone is digits with the country code ("5511999990000"). Returns the chat id, or "" if the
// number isn't on WhatsApp.
func (k *Link) LookUp(phone string) (found string, err error) {
	defer k.guard("LookUp", &err)
	digits := DigitsOnly(phone)
	if len(digits) < 8 {
		return "", errors.New("number too short")
	}
	res, err := k.c.IsOnWhatsApp(context.Background(), []string{"+" + digits})
	if err != nil {
		return "", err
	}
	for _, r := range res {
		if r.IsIn {
			if !r.PhoneNumber.IsEmpty() {
				return r.PhoneNumber.ToNonAD().String(), nil
			}
			return types.NewJID(digits, types.DefaultUserServer).String(), nil
		}
	}
	return "", nil
}

// DigitsOnly keeps the digits of a typed phone number ("+55 (11) 99999-0000" → "5511999990000").
func DigitsOnly(s string) string {
	b := make([]byte, 0, len(s))
	for i := 0; i < len(s); i++ {
		if s[i] >= '0' && s[i] <= '9' {
			b = append(b, s[i])
		}
	}
	return string(b)
}

func (k *Link) remember(id string, d whatsmeow.DownloadableMessage) {
	k.mu.Lock()
	defer k.mu.Unlock()
	if k.media == nil {
		k.media = map[string]whatsmeow.DownloadableMessage{}
	}
	if _, ok := k.media[id]; !ok {
		k.mediaOrder = append(k.mediaOrder, id)
	}
	k.media[id] = d
	for len(k.mediaOrder) > MaxPendingMedia {
		delete(k.media, k.mediaOrder[0])
		k.mediaOrder = k.mediaOrder[1:]
	}
}

// Download returns the decrypted bytes of a received photo, voice note or sticker. The app seals
// them before they touch the disk. It works for messages received since the link opened.
func (k *Link) Download(id string) (data []byte, err error) {
	defer k.guard("Download", &err)
	k.mu.Lock()
	d := k.media[id]
	k.mu.Unlock()
	if d == nil {
		return nil, errors.New("no media for this message")
	}
	return k.c.Download(context.Background(), d)
}

// SendImage sends a JPEG photo (the app has already re-encoded it, so it carries no location).
// Same rules as SendTextWithID: read-only, pace, ban, never to the parent's own number, one id.
func (k *Link) SendImage(chat string, jpeg []byte, caption, id string) (sent string, err error) {
	defer k.guard("SendImage", &err)
	w, h, thumb := jpegInfo(jpeg)
	return k.sendMedia(chat, id, func(up whatsmeow.UploadResponse) *waE2E.Message {
		m := ImageMessage(up, caption)
		if w > 0 {
			m.ImageMessage.Width, m.ImageMessage.Height = proto.Uint32(uint32(w)), proto.Uint32(uint32(h))
		}
		if len(thumb) > 0 {
			m.ImageMessage.JPEGThumbnail = thumb
		}
		return m
	}, jpeg, whatsmeow.MediaImage)
}

// SendReaction puts the kid's reaction on a message (v0.33); an empty emoji takes it away. sender is
// who wrote that message in a group ("" for the kid's own, or in a one-to-one chat the other person).
// Same rules as every send: allowed chats only (the app checks), read-only off, the pace.
func (k *Link) SendReaction(chat, targetID, sender, emoji, id string) (sent string, err error) {
	defer k.guard("SendReaction", &err)
	to, err := k.target(chat)
	if err != nil {
		return "", err
	}
	if err := k.maySend(); err != nil {
		return "", err
	}
	// An empty sender marks the message as ours (the kid's); whatsmeow builds the key from it.
	who := types.EmptyJID
	if sender != "" {
		if who, err = types.ParseJID(sender); err != nil {
			return "", err
		}
	}
	msg := k.c.BuildReaction(to, who, types.MessageID(targetID), emoji)
	_, err = k.c.SendMessage(context.Background(), to, msg, whatsmeow.SendRequestExtra{ID: types.MessageID(id)})
	return id, err
}

// ReceiptOf reads a receipt as the kid sees it: "delivered", "read", or "" for one that says nothing
// to the kid (the parent's own devices, retries, server notes). A played voice note counts as read.
func ReceiptOf(r *events.Receipt) string {
	if r == nil || r.IsFromMe {
		return ""
	}
	switch r.Type {
	case types.ReceiptTypeDelivered:
		return "delivered"
	case types.ReceiptTypeRead, types.ReceiptTypePlayed:
		return "read"
	}
	return ""
}

// SendVideo sends an MP4 the app prepared (720p at most, v0.28), or a GIF in WhatsApp's form when gif
// is true. Same rules as every send: allowed chats only, the pace, read-only off.
func (k *Link) SendVideo(chat string, mp4 []byte, seconds, width, height int, thumb []byte, gif bool, caption, id string) (sent string, err error) {
	defer k.guard("SendVideo", &err)
	return k.sendMedia(chat, id, func(up whatsmeow.UploadResponse) *waE2E.Message {
		return VideoMessage(up, seconds, width, height, thumb, gif, caption)
	}, mp4, whatsmeow.MediaVideo)
}

// SendVoice sends a voice note. mimetype is "audio/ogg; codecs=opus" for WhatsApp's own voice notes.
func (k *Link) SendVoice(chat string, audio []byte, seconds int, mimetype, id string) (sent string, err error) {
	defer k.guard("SendVoice", &err)
	// v0.32: an Ogg recording is re-packed into a standard Ogg Opus stream, with its length from the
	// audio itself (WhatsApp couldn't play what Android's writer produced). If it isn't Ogg Opus after
	// all, it's refused rather than sent broken.
	if strings.HasPrefix(mimetype, "audio/ogg") {
		clean, secs, nerr := normalizeOpus(audio)
		if nerr != nil {
			return "", nerr
		}
		audio, seconds = clean, secs
	}
	return k.sendMedia(chat, id, func(up whatsmeow.UploadResponse) *waE2E.Message {
		return VoiceMessage(up, seconds, mimetype)
	}, audio, whatsmeow.MediaAudio)
}

func (k *Link) sendMedia(chat, id string, build func(whatsmeow.UploadResponse) *waE2E.Message, data []byte, kind whatsmeow.MediaType) (string, error) {
	to, err := k.target(chat)
	if err != nil {
		return "", err
	}
	if err := k.maySend(); err != nil {
		return "", err
	}
	up, err := k.c.Upload(context.Background(), data, kind)
	if err != nil {
		return "", err
	}
	_, err = k.c.SendMessage(context.Background(), to, build(up), whatsmeow.SendRequestExtra{ID: types.MessageID(id)})
	return id, err
}

// target parses an allowed chat and refuses the parent's own number (rule L2).
func (k *Link) target(chat string) (types.JID, error) {
	to, err := types.ParseJID(chat)
	if err != nil {
		return to, err
	}
	if own := k.c.Store.ID; own != nil && to.User == own.User && to.Server == types.DefaultUserServer {
		// Rule L2, decision 0007: the parent's own chat only when the parent made it the line to the kid.
		k.mu.Lock()
		line := k.parentLine
		k.mu.Unlock()
		if line != to.ToNonAD().String() {
			return to, errors.New("never to the parent's own number")
		}
	}
	return to, nil
}

// SendSelfNote sends Oiê's answer to a parent's command ("/Tito parar") to the parent's own chat
// ("Mensagem para você"), so nobody else sees it (v0.21, the owner's choice). It's the only thing
// ever sent there (rule L2): the kid can never write to that chat. Same pace and read-only rules as
// every send.
func (k *Link) SendSelfNote(text string) (sent string, err error) {
	defer k.guard("SendSelfNote", &err)
	own := k.c.Store.ID
	if own == nil {
		return "", ErrNotLinked
	}
	if err := k.maySend(); err != nil {
		return "", err
	}
	id := k.c.GenerateMessageID()
	_, err = k.c.SendMessage(context.Background(), own.ToNonAD(), &waE2E.Message{Conversation: proto.String(text)}, whatsmeow.SendRequestExtra{ID: id})
	return string(id), err
}

// PictureMaxBytes caps a profile picture download (a preview is a few KB).
const PictureMaxBytes = 1 << 20

// Picture is an allowed contact's WhatsApp profile picture, preview size (v0.26). knownID is the
// picture id the app already has, so an unchanged picture isn't downloaded again. Returns JSON:
// {"state":"same"} | {"state":"none"} (no picture, or hidden from the parent's number) |
// {"state":"new","id":"…","jpeg":"base64"}. The app asks only for contacts the parent allowed.
func (k *Link) Picture(chat, knownID string) (out string, err error) {
	defer k.guard("Picture", &err)
	if !k.Paired() {
		return "", ErrNotLinked
	}
	jid, err := types.ParseJID(chat)
	if err != nil {
		return "", err
	}
	info, err := k.c.GetProfilePictureInfo(context.Background(), jid, &whatsmeow.GetProfilePictureParams{Preview: true, ExistingID: knownID})
	switch {
	case errors.Is(err, whatsmeow.ErrProfilePictureNotSet), errors.Is(err, whatsmeow.ErrProfilePictureUnauthorized):
		return encodePicture("none", "", nil)
	case err != nil:
		return "", err
	case info == nil || info.URL == "":
		return encodePicture("same", knownID, nil)
	}
	ctx, cancel := context.WithTimeout(context.Background(), 20*time.Second)
	defer cancel()
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, info.URL, nil)
	if err != nil {
		return "", err
	}
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("picture: http %d", resp.StatusCode)
	}
	body, err := io.ReadAll(io.LimitReader(resp.Body, PictureMaxBytes))
	if err != nil {
		return "", err
	}
	return encodePicture("new", info.ID, body)
}

func encodePicture(state, id string, jpeg []byte) (string, error) {
	out := map[string]string{"state": state}
	if id != "" && state != "none" {
		out["id"] = id
	}
	if len(jpeg) > 0 {
		out["jpeg"] = base64.StdEncoding.EncodeToString(jpeg)
	}
	b, err := json.Marshal(out)
	return string(b), err
}

// OwnChat is the parent's own chat ("Mensagem para você"), by phone number; "" if not linked.
func (k *Link) OwnChat() string {
	if own := k.c.Store.ID; own != nil {
		return own.ToNonAD().String()
	}
	return ""
}

// statusKey is a family member's status as a reaction or a reply points at it (v0.36).
func statusKey(poster types.JID, statusID string) *waCommon.MessageKey {
	return &waCommon.MessageKey{
		RemoteJID: proto.String(types.StatusBroadcastJID.String()), FromMe: proto.Bool(false),
		ID: proto.String(statusID), Participant: proto.String(poster.String()),
	}
}

// StatusReaction is the kid's reaction to a status; an empty emoji takes it away.
func StatusReaction(poster types.JID, statusID, emoji string, at time.Time) *waE2E.Message {
	return &waE2E.Message{ReactionMessage: &waE2E.ReactionMessage{
		Key: statusKey(poster, statusID), Text: proto.String(emoji), SenderTimestampMS: proto.Int64(at.UnixMilli()),
	}}
}

// StatusReply is the kid's answer to a status, quoting it, as WhatsApp's own "reply to status".
func StatusReply(poster types.JID, statusID, text string) *waE2E.Message {
	return &waE2E.Message{ExtendedTextMessage: &waE2E.ExtendedTextMessage{
		Text: proto.String(text),
		ContextInfo: &waE2E.ContextInfo{
			StanzaID: proto.String(statusID), Participant: proto.String(poster.String()),
			RemoteJID: proto.String(types.StatusBroadcastJID.String()),
		},
	}}
}

// SendStatusReaction reacts to a family member's status. It goes to the poster's own chat, never to the
// status broadcast (that would reach the parent's whole status audience). Same rules as every send.
func (k *Link) SendStatusReaction(poster, statusID, emoji, id string) (sent string, err error) {
	defer k.guard("SendStatusReaction", &err)
	to, err := k.target(poster)
	if err != nil {
		return "", err
	}
	if err := k.maySend(); err != nil {
		return "", err
	}
	_, err = k.c.SendMessage(context.Background(), to, StatusReaction(to, statusID, emoji, time.Now()), whatsmeow.SendRequestExtra{ID: types.MessageID(id)})
	return id, err
}

// SendStatusReply answers a family member's status in the poster's own chat, quoting the status.
func (k *Link) SendStatusReply(poster, statusID, text, id string) (sent string, err error) {
	defer k.guard("SendStatusReply", &err)
	to, err := k.target(poster)
	if err != nil {
		return "", err
	}
	if err := k.maySend(); err != nil {
		return "", err
	}
	_, err = k.c.SendMessage(context.Background(), to, StatusReply(to, statusID, text), whatsmeow.SendRequestExtra{ID: types.MessageID(id)})
	return id, err
}

// MarkStatusViewed tells the poster their status was seen (the owner's choice, v0.36): they see the
// parent's account in the viewers, since the tablet is the parent's WhatsApp. A receipt, not a message.
func (k *Link) MarkStatusViewed(poster, statusID string, atMillis int64) (err error) {
	defer k.guard("MarkStatusViewed", &err)
	from, err := types.ParseJID(poster)
	if err != nil {
		return err
	}
	return k.c.MarkRead(context.Background(), []types.MessageID{types.MessageID(statusID)}, time.UnixMilli(atMillis), types.StatusBroadcastJID, from)
}

// MaxStatusAudience is the most people a kid's status can go to (Contatos is a family, not a crowd).
const MaxStatusAudience = 64

// StatusAudience reads the people a kid's status goes to (v0.37): JIDs, comma-separated, from the app's
// allowed and unpaused people. Only people: never a group, a broadcast list or the parent's own number.
// An empty list is refused, so a status can never fall back to the parent's whole status audience.
func StatusAudience(csv string, own types.JID) ([]types.JID, error) {
	seen := map[types.JID]bool{}
	var out []types.JID
	for _, part := range strings.Split(csv, ",") {
		part = strings.TrimSpace(part)
		if part == "" {
			continue
		}
		jid, err := types.ParseJID(part)
		if err != nil {
			return nil, err
		}
		jid = jid.ToNonAD()
		if jid.Server != types.DefaultUserServer && jid.Server != types.HiddenUserServer {
			return nil, fmt.Errorf("a status goes only to people: %s", part)
		}
		if !own.IsEmpty() && jid.User == own.User {
			return nil, errors.New("never to the parent's own number")
		}
		if !seen[jid] {
			seen[jid] = true
			out = append(out, jid)
		}
	}
	if len(out) == 0 {
		return nil, errors.New("nobody to show the status to")
	}
	if len(out) > MaxStatusAudience {
		return nil, fmt.Errorf("at most %d people", MaxStatusAudience)
	}
	return out, nil
}

// TextStatus is a text status on a colour, as WhatsApp's own (the colours come from the app's tokens).
func TextStatus(text string, background, foreground uint32) *waE2E.Message {
	return &waE2E.Message{ExtendedTextMessage: &waE2E.ExtendedTextMessage{
		Text:           proto.String(text),
		BackgroundArgb: proto.Uint32(background),
		TextArgb:       proto.Uint32(foreground),
		Font:           waE2E.ExtendedTextMessage_SYSTEM.Enum(),
	}}
}

func (k *Link) ownJID() types.JID {
	if id := k.c.Store.ID; id != nil {
		return id.ToNonAD()
	}
	return types.EmptyJID
}

// sendStatus posts to status@broadcast for the chosen audience only (the whatsmeow patch,
// _whatsmeow-patch/broadcast.go). The audience is set and cleared around this one send.
func (k *Link) sendStatus(audience, id string, build func() (*waE2E.Message, error)) (string, error) {
	people, err := StatusAudience(audience, k.ownJID())
	if err != nil {
		return "", err
	}
	if err := k.maySend(); err != nil {
		return "", err
	}
	msg, err := build()
	if err != nil {
		return "", err
	}
	k.statusMu.Lock()
	defer k.statusMu.Unlock()
	k.c.SetStatusRecipients(people)
	defer k.c.SetStatusRecipients(nil)
	_, err = k.c.SendMessage(context.Background(), types.StatusBroadcastJID, msg, whatsmeow.SendRequestExtra{ID: types.MessageID(id)})
	return id, err
}

// PostStatusText posts the kid's text status (v0.37) to the people in audience only.
func (k *Link) PostStatusText(audience, text string, background, foreground int64, id string) (sent string, err error) {
	defer k.guard("PostStatusText", &err)
	return k.sendStatus(audience, id, func() (*waE2E.Message, error) {
		return TextStatus(text, uint32(background), uint32(foreground)), nil
	})
}

// PostStatusImage posts the kid's photo status (a JPEG the app re-encoded) to the people in audience only.
func (k *Link) PostStatusImage(audience string, jpeg []byte, caption, id string) (sent string, err error) {
	defer k.guard("PostStatusImage", &err)
	return k.sendStatus(audience, id, func() (*waE2E.Message, error) {
		up, err := k.c.Upload(context.Background(), jpeg, whatsmeow.MediaImage)
		if err != nil {
			return nil, err
		}
		m := ImageMessage(up, caption)
		if w, h, thumb := jpegInfo(jpeg); w > 0 {
			m.ImageMessage.Width, m.ImageMessage.Height = proto.Uint32(uint32(w)), proto.Uint32(uint32(h))
			if len(thumb) > 0 {
				m.ImageMessage.JPEGThumbnail = thumb
			}
		}
		return m, nil
	})
}

// PostStatusVideo posts the kid's video status (prepared on the tablet, a minute at most) to audience only.
func (k *Link) PostStatusVideo(audience string, mp4 []byte, seconds, width, height int, thumb []byte, caption, id string) (sent string, err error) {
	defer k.guard("PostStatusVideo", &err)
	return k.sendStatus(audience, id, func() (*waE2E.Message, error) {
		up, err := k.c.Upload(context.Background(), mp4, whatsmeow.MediaVideo)
		if err != nil {
			return nil, err
		}
		return VideoMessage(up, seconds, width, height, thumb, false, caption), nil
	})
}

// DeleteStatus takes the kid's status back, for the same people it went to ("Apagar para todos").
func (k *Link) DeleteStatus(audience, id string) (err error) {
	defer k.guard("DeleteStatus", &err)
	_, err = k.sendStatus(audience, string(k.c.GenerateMessageID()), func() (*waE2E.Message, error) {
		return k.c.BuildRevoke(types.StatusBroadcastJID, types.EmptyJID, types.MessageID(id)), nil
	})
	return err
}

// UnreadPatch is WhatsApp's own "mark as unread" for a chat, up to the message just sent from this
// account (v0.35). Kept separate from MarkUnread so it can be tested without a connection.
func UnreadPatch(chat types.JID, lastID string, at time.Time) appstate.PatchInfo {
	key := &waCommon.MessageKey{RemoteJID: proto.String(chat.String()), FromMe: proto.Bool(true), ID: proto.String(lastID)}
	return appstate.BuildMarkChatAsRead(chat, false, at, key)
}

// MarkUnread marks the line to the parent unread on all of the account's devices (v0.35): WhatsApp never
// notifies an account of its own messages, so the chat gets the unread dot and moves to the top instead.
// Only the parent line may be marked (the app passes it; anything else is refused), and nothing is sent to
// anyone: it's an app-state change, like "Marcar como não lida" on WhatsApp Web.
func (k *Link) MarkUnread(chat, lastID string, atMillis int64) (err error) {
	defer k.guard("MarkUnread", &err)
	k.mu.Lock()
	line := k.parentLine
	k.mu.Unlock()
	if line == "" || chat != line {
		return errors.New("only the line to the parent is marked unread")
	}
	jid, err := types.ParseJID(chat)
	if err != nil {
		return err
	}
	return k.c.SendAppState(context.Background(), UnreadPatch(jid, lastID, time.UnixMilli(atMillis)))
}

// OwnName is the WhatsApp profile name of the linked account (v0.34): setup fills in the parent's
// name with it, so it isn't typed twice. Empty when WhatsApp hasn't said it yet.
func (k *Link) OwnName() string {
	if k.c == nil || k.c.Store == nil {
		return ""
	}
	return k.c.Store.PushName
}

// SetParentLine tells the link which chat is the line between the kid and the parent who owns the
// WhatsApp (v0.30, decision 0007). Only then may the parent's own chat receive the kid's messages.
func (k *Link) SetParentLine(chat string) { k.mu.Lock(); k.parentLine = chat; k.mu.Unlock() }

// CreateParentLine creates a WhatsApp group with only the parent, named after the kid, for the kid to
// write to the parent (v0.30). Called only when the parent taps "Criar conversa com …". Returns the
// group's id; an error means WhatsApp refused it, and the app falls back to the parent's own chat.
func (k *Link) CreateParentLine(name string) (group string, err error) {
	defer k.guard("CreateParentLine", &err)
	if !k.Paired() {
		return "", ErrNotLinked
	}
	if err := k.maySend(); err != nil {
		return "", err
	}
	info, err := k.c.CreateGroup(context.Background(), whatsmeow.ReqCreateGroup{Name: name})
	if err != nil {
		return "", err
	}
	return info.JID.String(), nil
}

// IsSelfChat says whether chat is the parent's own chat, by phone number or by LID.
func IsSelfChat(chat, own, ownLID types.JID) bool {
	c := chat.ToNonAD()
	if c.Server == types.DefaultUserServer && c.User == own.User {
		return true
	}
	return !ownLID.IsEmpty() && c.Server == types.HiddenUserServer && c.User == ownLID.User
}

// SendSticker sends a WebP sticker as a real WhatsApp sticker. Same rules as every send.
func (k *Link) SendSticker(chat string, webp []byte, animated bool, id string) (sent string, err error) {
	defer k.guard("SendSticker", &err)
	return k.sendMedia(chat, id, func(up whatsmeow.UploadResponse) *waE2E.Message {
		if isLottie(webp) {
			return LottieStickerMessage(up)
		}
		return StickerMessage(up, animated)
	}, webp, whatsmeow.MediaImage)
}

// StickerMessage builds the message for an uploaded sticker (512 x 512 WebP, as WhatsApp's own).
func StickerMessage(up whatsmeow.UploadResponse, animated bool) *waE2E.Message {
	return &waE2E.Message{StickerMessage: &waE2E.StickerMessage{
		URL: proto.String(up.URL), DirectPath: proto.String(up.DirectPath), MediaKey: up.MediaKey,
		FileEncSHA256: up.FileEncSHA256, FileSHA256: up.FileSHA256, FileLength: proto.Uint64(up.FileLength),
		Mimetype: proto.String("image/webp"), Width: proto.Uint32(512), Height: proto.Uint32(512), IsAnimated: proto.Bool(animated),
	}}
}

// LottieStickerMessage is a WhatsApp store-pack animated sticker (v0.31): a Lottie animation in a zip,
// "application/was". Sent back the way it came, so it stays animated.
func LottieStickerMessage(up whatsmeow.UploadResponse) *waE2E.Message {
	return &waE2E.Message{StickerMessage: &waE2E.StickerMessage{
		URL: proto.String(up.URL), DirectPath: proto.String(up.DirectPath), MediaKey: up.MediaKey,
		FileEncSHA256: up.FileEncSHA256, FileSHA256: up.FileSHA256, FileLength: proto.Uint64(up.FileLength),
		Mimetype: proto.String("application/was"), Width: proto.Uint32(512), Height: proto.Uint32(512),
		IsAnimated: proto.Bool(true), IsLottie: proto.Bool(true),
	}}
}

// isLottie spots a Lottie sticker file: a zip ("PK\x03\x04").
func isLottie(b []byte) bool {
	return len(b) >= 4 && b[0] == 'P' && b[1] == 'K' && b[2] == 3 && b[3] == 4
}

// jpegInfo reads a photo's size and makes the small preview WhatsApp's own apps attach (at most
// 72 px on the long side). Zeroes and nil when the bytes aren't a JPEG it can read.
func jpegInfo(data []byte) (width, height int, thumb []byte) {
	img, err := jpeg.Decode(bytes.NewReader(data))
	if err != nil {
		return 0, 0, nil
	}
	b := img.Bounds()
	width, height = b.Dx(), b.Dy()
	tw, th := 72, 72
	if width >= height {
		th = max(1, height*72/width)
	} else {
		tw = max(1, width*72/height)
	}
	small := image.NewRGBA(image.Rect(0, 0, tw, th))
	for y := 0; y < th; y++ {
		for x := 0; x < tw; x++ {
			small.Set(x, y, img.At(b.Min.X+x*width/tw, b.Min.Y+y*height/th))
		}
	}
	var out bytes.Buffer
	if jpeg.Encode(&out, small, &jpeg.Options{Quality: 60}) == nil {
		thumb = out.Bytes()
	}
	return width, height, thumb
}

// ImageMessage builds the message for an uploaded photo.
func ImageMessage(up whatsmeow.UploadResponse, caption string) *waE2E.Message {
	m := &waE2E.ImageMessage{
		URL: proto.String(up.URL), DirectPath: proto.String(up.DirectPath), MediaKey: up.MediaKey,
		FileEncSHA256: up.FileEncSHA256, FileSHA256: up.FileSHA256, FileLength: proto.Uint64(up.FileLength),
		Mimetype: proto.String("image/jpeg"),
	}
	if caption != "" {
		m.Caption = proto.String(caption)
	}
	return &waE2E.Message{ImageMessage: m}
}

// VideoMessage builds the message for an uploaded MP4 (v0.28). gif sends it as WhatsApp's GIF: it
// loops, silent, labelled GIF. thumb is a small JPEG for the preview before it downloads.
func VideoMessage(up whatsmeow.UploadResponse, seconds, width, height int, thumb []byte, gif bool, caption string) *waE2E.Message {
	m := &waE2E.VideoMessage{
		URL: proto.String(up.URL), DirectPath: proto.String(up.DirectPath), MediaKey: up.MediaKey,
		FileEncSHA256: up.FileEncSHA256, FileSHA256: up.FileSHA256, FileLength: proto.Uint64(up.FileLength),
		Mimetype: proto.String("video/mp4"), Seconds: proto.Uint32(uint32(seconds)),
	}
	if width > 0 && height > 0 {
		m.Width, m.Height = proto.Uint32(uint32(width)), proto.Uint32(uint32(height))
	}
	if len(thumb) > 0 {
		m.JPEGThumbnail = thumb
	}
	if gif {
		m.GifPlayback = proto.Bool(true)
	}
	if caption != "" {
		m.Caption = proto.String(caption)
	}
	return &waE2E.Message{VideoMessage: m}
}

// VoiceMessage builds the message for an uploaded voice note (PTT: shown as a voice message).
func VoiceMessage(up whatsmeow.UploadResponse, seconds int, mimetype string) *waE2E.Message {
	return &waE2E.Message{AudioMessage: &waE2E.AudioMessage{
		URL: proto.String(up.URL), DirectPath: proto.String(up.DirectPath), MediaKey: up.MediaKey,
		FileEncSHA256: up.FileEncSHA256, FileSHA256: up.FileSHA256, FileLength: proto.Uint64(up.FileLength),
		Mimetype: proto.String(mimetype), Seconds: proto.Uint32(uint32(seconds)), PTT: proto.Bool(true),
	}}
}

func downloadable(m *waE2E.Message) whatsmeow.DownloadableMessage {
	switch {
	case m.GetImageMessage() != nil:
		return m.GetImageMessage()
	case m.GetAudioMessage() != nil:
		return m.GetAudioMessage()
	case m.GetStickerMessage() != nil:
		return m.GetStickerMessage()
	case m.GetVideoMessage() != nil:
		return m.GetVideoMessage()
	}
	return nil
}

func (k *Link) emit(kind string, payload any) {
	b, _ := json.Marshal(payload)
	k.l.OnEvent(kind, string(b))
}

func contactName(c types.ContactInfo) string {
	for _, n := range []string{c.FullName, c.FirstName, c.PushName, c.BusinessName} {
		if n != "" {
			return n
		}
	}
	return ""
}

// Incoming is one message as the app gets it, matching the Kotlin Message and Kind.
type Incoming struct {
	ID   string `json:"id"`
	Chat string `json:"chat"`
	// ChatAlt is the chat's other address (phone number or LID), when WhatsApp gives one: the app
	// matches the allowed list against both.
	ChatAlt string `json:"chatAlt,omitempty"`
	Sender  string `json:"sender"`
	// SenderAlt is the sender's other address in a group (phone number or LID), so the app can tell
	// whether they're on the kid's list; SenderName is their WhatsApp profile name, shown when they
	// aren't (v0.46, groups as groups).
	SenderAlt  string `json:"senderAlt,omitempty"`
	SenderName string `json:"senderName,omitempty"`
	// FromMe means sent by the parent's account: from the tablet (the kid) or from the parent's
	// own phone. The app tells them apart by the ids it sent; the parent's own are dropped.
	FromMe bool `json:"fromMe"`
	// SelfChat is the parent's chat with themselves ("Message yourself"). The app takes only
	// stickers from it, for the kid to save (v0.10); nothing else from it is ever kept.
	SelfChat    bool   `json:"selfChat,omitempty"`
	At          int64  `json:"at"`
	Kind        string `json:"kind"`
	Text        string `json:"text,omitempty"`
	ReplyTo     string `json:"replyTo,omitempty"`
	DurationSec int    `json:"durationSec,omitempty"`
	// Animated is for stickers, so a saved one goes out the same way.
	Animated bool `json:"animated,omitempty"`
	// Size is the media's size in bytes (v0.27): the app doesn't download a video over its limit.
	Size int64 `json:"size,omitempty"`
	// Status marks a family member's status (v0.36). Chat is then the poster (who posted it), not
	// WhatsApp's "status broadcast"; the app keeps it only for allowed people and only when the parent
	// turned statuses on. The parent's own statuses never come through.
	Status bool `json:"status,omitempty"`
}

// ToIncoming translates a whatsmeow message for the app. ok is false for what the app never needs
// (edits, receipts-like protocol messages, status updates). Reactions come as Kind "REACTION" (v0.33).
func ToIncoming(info types.MessageInfo, m *waE2E.Message) (Incoming, bool) {
	status := info.Chat == types.StatusBroadcastJID
	if m == nil || (info.Chat.Server == types.BroadcastServer && !status) || (status && info.IsFromMe) {
		return Incoming{}, false
	}
	in := Incoming{
		ID:     string(info.ID),
		Chat:   info.Chat.ToNonAD().String(),
		Sender: info.Sender.ToNonAD().String(),
		FromMe: info.IsFromMe,
		At:     info.Timestamp.UnixMilli(),
	}
	alt := info.SenderAlt
	if info.IsFromMe {
		alt = info.RecipientAlt
	}
	if !info.IsGroup && !alt.IsEmpty() {
		in.ChatAlt = alt.ToNonAD().String()
	}
	if info.IsGroup && !info.IsFromMe {
		in.SenderName = info.PushName
		if !info.SenderAlt.IsEmpty() {
			in.SenderAlt = info.SenderAlt.ToNonAD().String()
		}
	}
	var ctx *waE2E.ContextInfo
	switch {
	case m.GetProtocolMessage() != nil:
		p := m.GetProtocolMessage()
		if p.GetType() != waE2E.ProtocolMessage_REVOKE {
			return Incoming{}, false
		}
		in.Kind, in.ReplyTo = "DELETED", p.GetKey().GetID()
	case m.Conversation != nil:
		in.Kind, in.Text = "TEXT", m.GetConversation()
	case m.GetExtendedTextMessage() != nil:
		t := m.GetExtendedTextMessage()
		in.Kind, in.Text, ctx = "TEXT", t.GetText(), t.GetContextInfo()
	case m.GetImageMessage() != nil:
		i := m.GetImageMessage()
		in.Kind, in.Text, ctx = "PHOTO", i.GetCaption(), i.GetContextInfo()
	case m.GetAudioMessage() != nil:
		a := m.GetAudioMessage()
		in.Kind, in.DurationSec, ctx = "VOICE", int(a.GetSeconds()), a.GetContextInfo()
	case m.GetStickerMessage() != nil:
		s := m.GetStickerMessage()
		in.Kind, ctx, in.Animated = "STICKER", s.GetContextInfo(), s.GetIsAnimated()
	case m.GetVideoMessage() != nil:
		// v0.27: WhatsApp sends a GIF as a short silent video with GifPlayback set.
		v := m.GetVideoMessage()
		in.Kind, in.Text, in.DurationSec, in.Size, ctx = "VIDEO", v.GetCaption(), int(v.GetSeconds()), int64(v.GetFileLength()), v.GetContextInfo()
		if v.GetGifPlayback() {
			in.Kind = "GIF"
		}
	case m.GetReactionMessage() != nil:
		// v0.33: a reaction from the family shows under the message it's on. ReplyTo is that message;
		// an empty Text takes the reaction away.
		r := m.GetReactionMessage()
		in.Kind, in.Text, in.ReplyTo = "REACTION", r.GetText(), r.GetKey().GetID()
		return in, true
	case m.GetContactMessage() != nil:
		// v0.43: a shared contact card, read by the app only after the parent's "/Tito adicionar" in their own
		// chat. Anywhere else the app shows it as "só abre no WhatsApp", as before.
		c := m.GetContactMessage()
		in.Kind, in.Text, ctx = "CONTACT", c.GetVcard(), c.GetContextInfo()
	case m.GetContactsArrayMessage() != nil:
		a := m.GetContactsArrayMessage()
		cards := make([]string, 0, len(a.GetContacts()))
		for _, c := range a.GetContacts() {
			cards = append(cards, c.GetVcard())
		}
		in.Kind, in.Text, ctx = "CONTACT", strings.Join(cards, "\n"), a.GetContextInfo()
	case m.GetEditedMessage() != nil, keysOnly(m):
		return Incoming{}, false
	default:
		in.Kind = "UNSUPPORTED" // a document, a location, a poll: "só abre no WhatsApp"
	}
	if ctx != nil {
		in.ReplyTo = ctx.GetStanzaID()
	}
	if status {
		// A status belongs to its poster: the app matches it to an allowed contact like a chat.
		in.Status, in.Chat, in.ReplyTo = true, info.Sender.ToNonAD().String(), ""
	}
	return in, true
}

// keysOnly spots a message that carries only group encryption keys, nothing to show.
func keysOnly(m *waE2E.Message) bool {
	if m.GetSenderKeyDistributionMessage() == nil {
		return false
	}
	c := proto.Clone(m).(*waE2E.Message)
	c.SenderKeyDistributionMessage = nil
	c.MessageContextInfo = nil
	return proto.Size(c) == 0
}
