package oielink

import (
	"bytes"
	"context"
	"database/sql"
	"errors"
	"fmt"
	"image"
	"image/jpeg"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"go.mau.fi/whatsmeow"
	"go.mau.fi/whatsmeow/proto/waCommon"
	"go.mau.fi/whatsmeow/proto/waE2E"
	"go.mau.fi/whatsmeow/store"
	"go.mau.fi/whatsmeow/types"
	"go.mau.fi/whatsmeow/types/events"
	"google.golang.org/protobuf/proto"
)

type nl struct{}

func (nl) OnEvent(k, p string) {}

func TestOpenFreshStoreIsUnpaired(t *testing.T) {
	k, err := Open(filepath.Join(t.TempDir(), "s.db"), nl{})
	if err != nil {
		t.Fatal(err)
	}
	if k.Paired() {
		t.Fatal("a fresh store must be unpaired")
	}
}

func TestDeviceNameAndHistoryLimits(t *testing.T) {
	if store.DeviceProps.GetOs() != "Oiê" {
		t.Fatalf("Linked devices must show Oiê, got %q", store.DeviceProps.GetOs())
	}
	h := store.DeviceProps.GetHistorySyncConfig()
	if h.GetFullSyncDaysLimit() != 1 || h.GetRecentSyncDaysLimit() != 1 {
		t.Fatal("history must be limited to 1 day")
	}
}

func info(id string, fromMe bool) types.MessageInfo {
	return types.MessageInfo{
		MessageSource: types.MessageSource{
			Chat:     types.NewJID("5511999990000", types.DefaultUserServer),
			Sender:   types.NewJID("5511999990000", types.DefaultUserServer),
			IsFromMe: fromMe,
		},
		ID:        types.MessageID(id),
		Timestamp: time.UnixMilli(1_795_020_000_000),
	}
}

func TestToIncomingKinds(t *testing.T) {
	cases := []struct {
		name string
		m    *waE2E.Message
		want Incoming
	}{
		{"text", &waE2E.Message{Conversation: proto.String("Tito, oi")}, Incoming{Kind: "TEXT", Text: "Tito, oi"}},
		{"reply", &waE2E.Message{ExtendedTextMessage: &waE2E.ExtendedTextMessage{Text: proto.String("sim"), ContextInfo: &waE2E.ContextInfo{StanzaID: proto.String("k1")}}}, Incoming{Kind: "TEXT", Text: "sim", ReplyTo: "k1"}},
		{"photo", &waE2E.Message{ImageMessage: &waE2E.ImageMessage{Caption: proto.String("olha")}}, Incoming{Kind: "PHOTO", Text: "olha"}},
		{"voice", &waE2E.Message{AudioMessage: &waE2E.AudioMessage{Seconds: proto.Uint32(7), PTT: proto.Bool(true)}}, Incoming{Kind: "VOICE", DurationSec: 7}},
		{"sticker", &waE2E.Message{StickerMessage: &waE2E.StickerMessage{}}, Incoming{Kind: "STICKER"}},
		{"deleted", &waE2E.Message{ProtocolMessage: &waE2E.ProtocolMessage{Type: waE2E.ProtocolMessage_REVOKE.Enum(), Key: &waCommon.MessageKey{ID: proto.String("m9")}}}, Incoming{Kind: "DELETED", ReplyTo: "m9"}},
		{"video", &waE2E.Message{VideoMessage: &waE2E.VideoMessage{Caption: proto.String("Tito, olha"), Seconds: proto.Uint32(42)}}, Incoming{Kind: "VIDEO", Text: "Tito, olha", DurationSec: 42}},
		{"gif", &waE2E.Message{VideoMessage: &waE2E.VideoMessage{GifPlayback: proto.Bool(true), Seconds: proto.Uint32(3)}}, Incoming{Kind: "GIF", DurationSec: 3}},
		{"location", &waE2E.Message{LocationMessage: &waE2E.LocationMessage{}}, Incoming{Kind: "UNSUPPORTED"}},
	}
	for _, c := range cases {
		got, ok := ToIncoming(info("m1", false), c.m)
		if !ok {
			t.Fatalf("%s: dropped", c.name)
		}
		if got.Kind != c.want.Kind || got.Text != c.want.Text || got.ReplyTo != c.want.ReplyTo || got.DurationSec != c.want.DurationSec {
			t.Fatalf("%s: got %+v", c.name, got)
		}
		if got.ID != "m1" || got.Chat != "5511999990000@s.whatsapp.net" || got.At != 1_795_020_000_000 {
			t.Fatalf("%s: wrong id, chat or time: %+v", c.name, got)
		}
	}
}

func TestToIncomingDropsWhatTheAppNeverNeeds(t *testing.T) {
	drops := map[string]*waE2E.Message{
		"keys":     {SenderKeyDistributionMessage: &waE2E.SenderKeyDistributionMessage{}},
		"edit-ish": {ProtocolMessage: &waE2E.ProtocolMessage{Type: waE2E.ProtocolMessage_MESSAGE_EDIT.Enum()}},
	}
	for name, m := range drops {
		if _, ok := ToIncoming(info("x", false), m); ok {
			t.Fatalf("%s should be dropped", name)
		}
	}
	own := info("s", true)
	own.Chat = types.StatusBroadcastJID
	if _, ok := ToIncoming(own, &waE2E.Message{Conversation: proto.String("hi")}); ok {
		t.Fatal("the parent's own statuses never come through (v0.36)")
	}
	list := info("b", false)
	list.Chat = types.NewJID("1234", types.BroadcastServer)
	if _, ok := ToIncoming(list, &waE2E.Message{Conversation: proto.String("hi")}); ok {
		t.Fatal("broadcast lists other than status are dropped")
	}
}

func TestReadOnlyAndPaceBlockSends(t *testing.T) {
	k, err := Open(filepath.Join(t.TempDir(), "s.db"), nl{})
	if err != nil {
		t.Fatal(err)
	}
	k.SetReadOnly(true)
	if err := k.maySend(); err != ErrReadOnly {
		t.Fatalf("read-only must block, got %v", err)
	}
	k.SetReadOnly(false)
	if err := k.maySend(); err != nil {
		t.Fatal(err)
	}
	if err := k.maySend(); err != ErrTooFast {
		t.Fatalf("a second send within 2 s must wait, got %v", err)
	}
	k.stop()
	if err := k.maySend(); err != ErrStopped {
		t.Fatalf("after a ban nothing sends, got %v", err)
	}
}

func TestMediaMessagesCarryTheUpload(t *testing.T) {
	up := whatsmeow.UploadResponse{URL: "https://u", DirectPath: "/d", MediaKey: []byte{1}, FileEncSHA256: []byte{2}, FileSHA256: []byte{3}, FileLength: 42}
	img := ImageMessage(up, "olha").GetImageMessage()
	if img.GetDirectPath() != "/d" || img.GetFileLength() != 42 || img.GetMimetype() != "image/jpeg" || img.GetCaption() != "olha" {
		t.Fatalf("image: %+v", img)
	}
	vid := VideoMessage(up, 42, 1280, 720, []byte{9}, false, "*Tito*").GetVideoMessage()
	if vid.GetMimetype() != "video/mp4" || vid.GetSeconds() != 42 || vid.GetWidth() != 1280 || vid.GetGifPlayback() || len(vid.GetJPEGThumbnail()) != 1 || vid.GetCaption() != "*Tito*" {
		t.Fatalf("video: %+v", vid)
	}
	if !VideoMessage(up, 3, 0, 0, nil, true, "").GetVideoMessage().GetGifPlayback() {
		t.Fatal("a GIF goes with GifPlayback, so it loops in WhatsApp")
	}
	v := VoiceMessage(up, 7, "audio/ogg; codecs=opus").GetAudioMessage()
	if !v.GetPTT() || v.GetSeconds() != 7 || v.GetMimetype() != "audio/ogg; codecs=opus" {
		t.Fatalf("voice: %+v", v)
	}
}

func TestPendingMediaIsBounded(t *testing.T) {
	k := &Link{}
	for i := 0; i < MaxPendingMedia+5; i++ {
		k.remember(string(rune('a'+i%26))+time.Duration(i).String(), &waE2E.ImageMessage{})
	}
	if len(k.media) != MaxPendingMedia || len(k.mediaOrder) != MaxPendingMedia {
		t.Fatalf("kept %d", len(k.media))
	}
	if _, err := k.Download("nope"); err == nil {
		t.Fatal("unknown media must fail")
	}
}

func TestOwnNumberRefused(t *testing.T) {
	k, _ := Open(filepath.Join(t.TempDir(), "s.db"), nl{})
	own := types.NewJID("5511900000000", types.DefaultUserServer)
	k.c.Store.ID = &own
	if _, err := k.target("5511900000000@s.whatsapp.net"); err == nil {
		t.Fatal("the parent's own number must be refused")
	}
	if _, err := k.target("5511900000001@s.whatsapp.net"); err != nil {
		t.Fatal(err)
	}
}

func TestSelfChatByNumberOrLID(t *testing.T) {
	own := types.NewJID("5511900000000", types.DefaultUserServer)
	lid := types.NewJID("12345", types.HiddenUserServer)
	if !IsSelfChat(types.NewJID("5511900000000", types.DefaultUserServer), own, lid) || !IsSelfChat(types.NewJID("12345", types.HiddenUserServer), own, lid) {
		t.Fatal("the parent's own chat must be recognised by number and by LID")
	}
	if IsSelfChat(types.NewJID("5511900000001", types.DefaultUserServer), own, lid) || IsSelfChat(types.NewJID("5511900000000", types.GroupServer), own, lid) {
		t.Fatal("other chats and groups are not the self chat")
	}
}

func TestStickerMessageIsARealSticker(t *testing.T) {
	s := StickerMessage(whatsmeow.UploadResponse{DirectPath: "/d", FileLength: 9}, true).GetStickerMessage()
	if s.GetMimetype() != "image/webp" || !s.GetIsAnimated() || s.GetWidth() != 512 || s.GetDirectPath() != "/d" {
		t.Fatalf("sticker: %+v", s)
	}
	in, ok := ToIncoming(info("s1", true), &waE2E.Message{StickerMessage: &waE2E.StickerMessage{IsAnimated: proto.Bool(true)}})
	if !ok || in.Kind != "STICKER" || !in.Animated {
		t.Fatalf("incoming sticker: %+v", in)
	}
}

func TestDigitsOnly(t *testing.T) {
	if got := DigitsOnly("+55 (11) 99999-0000"); got != "5511999990000" {
		t.Fatal(got)
	}
}

func TestContactsChangedIsOncePerBurst(t *testing.T) {
	got := make(chan string, 10)
	k := &Link{l: listenerFunc(func(kind, _ string) { got <- kind })}
	for i := 0; i < 5; i++ {
		k.contactsChanged()
	}
	select {
	case kind := <-got:
		if kind != "contactsChanged" {
			t.Fatal(kind)
		}
	case <-time.After(5 * time.Second):
		t.Fatal("no event")
	}
	select {
	case <-got:
		t.Fatal("a burst must give one event")
	case <-time.After(2500 * time.Millisecond):
	}
}

type listenerFunc func(kind, payload string)

func (f listenerFunc) OnEvent(kind, payload string) { f(kind, payload) }

func TestGuardTurnsAPanicIntoAnError(t *testing.T) {
	got := make(chan string, 1)
	k := &Link{l: listenerFunc(func(kind, payload string) {
		if kind == "log" {
			got <- payload
		}
	})}
	f := func() (err error) {
		defer k.guard("probe", &err)
		var m map[string]int
		m["boom"] = 1 // a nil-map write panics
		return nil
	}
	if err := f(); err == nil || !strings.Contains(err.Error(), "probe") {
		t.Fatalf("want an error naming the place, got %v", err)
	}
	if p := <-got; !strings.Contains(p, "panic") {
		t.Fatalf("the panic must be reported: %s", p)
	}
}

func TestJPEGInfoGivesSizeAndASmallPreview(t *testing.T) {
	img := image.NewRGBA(image.Rect(0, 0, 400, 200))
	var b bytes.Buffer
	if err := jpeg.Encode(&b, img, nil); err != nil {
		t.Fatal(err)
	}
	w, h, thumb := jpegInfo(b.Bytes())
	if w != 400 || h != 200 || len(thumb) == 0 {
		t.Fatalf("got %dx%d, %d bytes", w, h, len(thumb))
	}
	tc, err := jpeg.DecodeConfig(bytes.NewReader(thumb))
	if err != nil || tc.Width != 72 || tc.Height != 36 {
		t.Fatalf("preview %+v %v", tc, err)
	}
	if w, _, _ := jpegInfo([]byte("not a jpeg")); w != 0 {
		t.Fatal("non-JPEG must give zeroes")
	}
}

func TestOpenSetsUpTheCrashFile(t *testing.T) {
	db := filepath.Join(t.TempDir(), "s.db")
	if _, err := Open(db, nl{}); err != nil {
		t.Fatal(err)
	}
	if _, err := os.Stat(db + CrashSuffix); err != nil {
		t.Fatalf("the crash file must exist next to the store: %v", err)
	}
}

func TestNoContactsIsAnEmptyListNotNull(t *testing.T) {
	if got, _ := encodeContacts(nil); got != "[]" {
		t.Fatalf("got %s: the app reads this as a list", got)
	}
	got, _ := encodeContacts([]contactEntry{{"b", "Bia", false, ""}, {"a", "Ana", false, "5511987650001"}})
	if got != `[{"id":"a","name":"Ana","isGroup":false,"phone":"5511987650001"},{"id":"b","name":"Bia","isGroup":false}]` {
		t.Fatal(got)
	}
}

func TestPhoneComesFromANumberIDOnly(t *testing.T) {
	if got := phoneDigits(types.NewJID("5511987650001", types.DefaultUserServer)); got != "5511987650001" {
		t.Fatal(got)
	}
	for _, j := range []types.JID{types.NewJID("123456789", types.HiddenUserServer), types.NewJID("120363", types.GroupServer)} {
		if got := phoneDigits(j); got != "" {
			t.Fatalf("%s gave %q: a private id or a group has no number", j, got)
		}
	}
}

func TestPhoneOfAPrivateIDWithoutAMappingIsEmpty(t *testing.T) {
	k, err := Open(filepath.Join(t.TempDir(), "w.db"), nl{})
	if err != nil {
		t.Fatal(err)
	}
	if got := k.phoneOf(t.Context(), types.NewJID("123456789", types.HiddenUserServer)); got != "" {
		t.Fatal(got)
	}
}

func TestAlreadyConnectedIsNotAFailure(t *testing.T) {
	if !alreadyUp(whatsmeow.ErrAlreadyConnected) || !alreadyUp(fmt.Errorf("connect: %w", whatsmeow.ErrAlreadyConnected)) {
		t.Fatal("already connected must count as connected")
	}
	if alreadyUp(errors.New("dial tcp: no route to host")) || alreadyUp(nil) {
		t.Fatal("a real failure is a failure")
	}
}

func TestFreshLinkIsNotConnected(t *testing.T) {
	k, err := Open(filepath.Join(t.TempDir(), "c.db"), nl{})
	if err != nil {
		t.Fatal(err)
	}
	if k.IsConnected() {
		t.Fatal("nothing connected yet")
	}
}

func TestSelfNoteNeedsALinkedTablet(t *testing.T) {
	k, err := Open(filepath.Join(t.TempDir(), "n.db"), nl{})
	if err != nil {
		t.Fatal(err)
	}
	if _, err := k.SendSelfNote("Oiê: ok"); !errors.Is(err, ErrNotLinked) {
		t.Fatalf("got %v", err)
	}
}

func TestTheStoreKeepsTemporaryTablesInMemory(t *testing.T) {
	db, err := sql.Open("sqlite", storeDSN(filepath.Join(t.TempDir(), "t.db")))
	if err != nil {
		t.Fatal(err)
	}
	defer db.Close()
	var mode int
	if err := db.QueryRow("PRAGMA temp_store").Scan(&mode); err != nil {
		t.Fatal(err)
	}
	if mode != 2 {
		t.Fatalf("temp_store = %d: on Android SQLite finds no temp folder (issue #34)", mode)
	}
}

func TestPictureNeedsALinkedTablet(t *testing.T) {
	k, err := Open(filepath.Join(t.TempDir(), "p.db"), nl{})
	if err != nil {
		t.Fatal(err)
	}
	if _, err := k.Picture("5511987650001@s.whatsapp.net", ""); !errors.Is(err, ErrNotLinked) {
		t.Fatalf("got %v", err)
	}
}

func TestPictureAnswers(t *testing.T) {
	for _, c := range []struct {
		state, id string
		jpeg      []byte
		want      string
	}{
		{"none", "x", nil, `{"state":"none"}`},
		{"same", "123", nil, `{"id":"123","state":"same"}`},
		{"new", "124", []byte{0xff, 0xd8}, `{"id":"124","jpeg":"/9g=","state":"new"}`},
	} {
		if got, _ := encodePicture(c.state, c.id, c.jpeg); got != c.want {
			t.Errorf("%s: got %s", c.state, got)
		}
	}
}

func TestVideoCarriesItsSize(t *testing.T) {
	m := &waE2E.Message{VideoMessage: &waE2E.VideoMessage{FileLength: proto.Uint64(70 << 20)}}
	got, _ := ToIncoming(info("v1", false), m)
	if got.Size != 70<<20 || downloadable(m) == nil {
		t.Fatalf("got %+v", got)
	}
}

func TestOwnChatOnlyWhenItIsTheParentLine(t *testing.T) {
	k, err := Open(filepath.Join(t.TempDir(), "o.db"), nl{})
	if err != nil {
		t.Fatal(err)
	}
	if k.OwnChat() != "" {
		t.Fatal("not linked: no own chat")
	}
	if _, err := k.CreateParentLine("Tito"); !errors.Is(err, ErrNotLinked) {
		t.Fatalf("got %v", err)
	}
	own := types.NewJID("5511900000000", types.DefaultUserServer)
	k.c.Store.ID = &own
	if _, err := k.target(own.String()); err == nil {
		t.Fatal("rule L2: never to the parent's own number")
	}
	k.SetParentLine(own.String())
	if _, err := k.target(own.String()); err != nil {
		t.Fatalf("the parent made their own chat the line: %v", err)
	}
	if k.OwnChat() != own.String() {
		t.Fatal(k.OwnChat())
	}
	k.c.Store.ID = nil
}

func TestALottieStickerGoesBackAsLottie(t *testing.T) {
	if !isLottie([]byte("PK\x03\x04rest")) || isLottie([]byte("RIFF....WEBP")) || isLottie(nil) {
		t.Fatal("a .was sticker is a zip")
	}
	s := LottieStickerMessage(whatsmeow.UploadResponse{FileLength: 9}).GetStickerMessage()
	if !s.GetIsLottie() || !s.GetIsAnimated() || s.GetMimetype() != "application/was" {
		t.Fatalf("%+v", s)
	}
}

func TestReactionsComeThrough(t *testing.T) {
	m := &waE2E.Message{ReactionMessage: &waE2E.ReactionMessage{Text: proto.String("❤"), Key: &waCommon.MessageKey{ID: proto.String("k1")}}}
	got, ok := ToIncoming(info("r1", false), m)
	if !ok || got.Kind != "REACTION" || got.Text != "❤" || got.ReplyTo != "k1" {
		t.Fatalf("got %+v %v", got, ok)
	}
	off := &waE2E.Message{ReactionMessage: &waE2E.ReactionMessage{Text: proto.String(""), Key: &waCommon.MessageKey{ID: proto.String("k1")}}}
	if got, _ := ToIncoming(info("r2", false), off); got.Text != "" || got.ReplyTo != "k1" {
		t.Fatalf("a removed reaction keeps its target: %+v", got)
	}
}

func TestReceiptOf(t *testing.T) {
	cases := []struct {
		typ    types.ReceiptType
		fromMe bool
		want   string
	}{
		{types.ReceiptTypeDelivered, false, "delivered"},
		{types.ReceiptTypeRead, false, "read"},
		{types.ReceiptTypePlayed, false, "read"},
		{types.ReceiptTypeReadSelf, false, ""},
		{types.ReceiptTypeRetry, false, ""},
		{types.ReceiptTypeRead, true, ""},
	}
	for _, c := range cases {
		r := &events.Receipt{Type: c.typ}
		r.IsFromMe = c.fromMe
		if got := ReceiptOf(r); got != c.want {
			t.Fatalf("%q fromMe=%v: got %q, want %q", c.typ, c.fromMe, got, c.want)
		}
	}
	if ReceiptOf(nil) != "" {
		t.Fatal("nil")
	}
}

func TestUnreadPatchMarksTheChatUnreadUpToTheMessage(t *testing.T) {
	chat := types.NewJID("120363000000000000", types.GroupServer)
	p := UnreadPatch(chat, "k1", time.UnixMilli(1_795_020_000_000))
	if len(p.Mutations) != 1 {
		t.Fatalf("one mutation, got %d", len(p.Mutations))
	}
	m := p.Mutations[0]
	if m.Index[1] != chat.String() {
		t.Fatalf("wrong chat: %v", m.Index)
	}
	a := m.Value.GetMarkChatAsReadAction()
	if a == nil || a.GetRead() {
		t.Fatalf("must be marked unread: %+v", a)
	}
	last := a.GetMessageRange().GetMessages()
	if len(last) != 1 || last[0].GetKey().GetID() != "k1" || !last[0].GetKey().GetFromMe() {
		t.Fatalf("the range must end at the kid's message: %+v", last)
	}
}

func TestMarkUnreadOnlyOnTheParentLine(t *testing.T) {
	k, err := Open(filepath.Join(t.TempDir(), "s.db"), nl{})
	if err != nil {
		t.Fatal(err)
	}
	if err := k.MarkUnread("5511999990000@s.whatsapp.net", "k1", 1); err == nil {
		t.Fatal("no line set: must refuse")
	}
	k.SetParentLine("120363000000000000@g.us")
	if err := k.MarkUnread("5511999990000@s.whatsapp.net", "k1", 1); err == nil {
		t.Fatal("another chat: must refuse")
	}
}

func TestAFamilyStatusComesThroughAsThePosters(t *testing.T) {
	i := info("st1", false)
	i.Chat = types.StatusBroadcastJID
	i.Sender = types.NewJID("5511999990000", types.DefaultUserServer)
	got, ok := ToIncoming(i, &waE2E.Message{ImageMessage: &waE2E.ImageMessage{Caption: proto.String("o bolo")}})
	if !ok || !got.Status || got.Chat != "5511999990000@s.whatsapp.net" || got.Kind != "PHOTO" || got.Text != "o bolo" {
		t.Fatalf("got %+v %v", got, ok)
	}
}

func TestStatusReactionAndReplyPointAtTheStatus(t *testing.T) {
	poster := types.NewJID("5511999990000", types.DefaultUserServer)
	r := StatusReaction(poster, "st1", "❤️", time.UnixMilli(1)).GetReactionMessage()
	k := r.GetKey()
	if k.GetRemoteJID() != "status@broadcast" || k.GetID() != "st1" || k.GetParticipant() != poster.String() || k.GetFromMe() || r.GetText() != "❤️" {
		t.Fatalf("reaction key: %+v", k)
	}
	c := StatusReply(poster, "st1", "Que lindo").GetExtendedTextMessage()
	if c.GetText() != "Que lindo" || c.GetContextInfo().GetStanzaID() != "st1" || c.GetContextInfo().GetRemoteJID() != "status@broadcast" || c.GetContextInfo().GetParticipant() != poster.String() {
		t.Fatalf("reply context: %+v", c.GetContextInfo())
	}
}

func TestAStatusGoesOnlyToPeople(t *testing.T) {
	own := types.NewJID("5511900000000", types.DefaultUserServer)
	got, err := StatusAudience(" 5511999990000@s.whatsapp.net,5511999990001:3@s.whatsapp.net, 5511999990000@s.whatsapp.net ,123@lid", own)
	if err != nil || len(got) != 3 || got[1].Device != 0 {
		t.Fatalf("got %v %v", got, err)
	}
	for _, bad := range []string{"", " , ", "120363000000000000@g.us", "status@broadcast", "5511900000000@s.whatsapp.net", "x@y@z"} {
		if _, err := StatusAudience(bad, own); err == nil {
			t.Fatalf("%q must be refused", bad)
		}
	}
	many := make([]string, MaxStatusAudience+1)
	for i := range many {
		many[i] = fmt.Sprintf("55119999%05d@s.whatsapp.net", i)
	}
	if _, err := StatusAudience(strings.Join(many, ","), own); err == nil {
		t.Fatal("too many must be refused")
	}
}

// The patch (_whatsmeow-patch/broadcast.go): a status goes to the chosen people and the account's own
// devices, and nothing else; clearing the choice goes back to whatsmeow's own behaviour.
func TestThePatchSendsAStatusOnlyToTheChosen(t *testing.T) {
	k, err := Open(filepath.Join(t.TempDir(), "s.db"), nl{})
	if err != nil {
		t.Fatal(err)
	}
	own := types.NewJID("5511900000000", types.DefaultUserServer)
	k.c.Store.ID = &own
	vo := types.NewJID("5511999990000", types.DefaultUserServer)
	k.c.SetStatusRecipients([]types.JID{vo})
	got, err := k.c.StatusRecipientsFor(context.Background())
	if err != nil || len(got) != 2 || got[0] != vo || got[1].User != own.User {
		t.Fatalf("got %v %v", got, err)
	}
	k.c.SetStatusRecipients(nil)
	if _, err := k.c.StatusRecipientsFor(context.Background()); err == nil {
		t.Fatal("without a choice it asks WhatsApp for the privacy list (offline here: an error), never the old list")
	}
}

func TestStatusPostsFollowTheSendRules(t *testing.T) {
	k, err := Open(filepath.Join(t.TempDir(), "s.db"), nl{})
	if err != nil {
		t.Fatal(err)
	}
	if _, err := k.PostStatusText("", "oi", 0, 0, "s1"); err == nil {
		t.Fatal("nobody: refused")
	}
	k.SetReadOnly(true)
	if _, err := k.PostStatusText("5511999990000@s.whatsapp.net", "oi", 0, 0, "s1"); !errors.Is(err, ErrReadOnly) {
		t.Fatalf("read-only: %v", err)
	}
	m := TextStatus("*Tito*: oi", 0xFFE8573C, 0xFF2A1A14).GetExtendedTextMessage()
	if m.GetBackgroundArgb() != 0xFFE8573C || m.GetTextArgb() != 0xFF2A1A14 || m.GetText() != "*Tito*: oi" {
		t.Fatalf("got %+v", m)
	}
}
