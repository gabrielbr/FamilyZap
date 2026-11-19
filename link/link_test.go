package oielink

import (
	"bytes"
	"image"
	"image/jpeg"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"go.mau.fi/whatsmeow"
	"go.mau.fi/whatsmeow/proto/waCommon"
	"go.mau.fi/whatsmeow/proto/waE2E"
	"go.mau.fi/whatsmeow/store"
	"go.mau.fi/whatsmeow/types"
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
		"reaction": {ReactionMessage: &waE2E.ReactionMessage{Text: proto.String("❤")}},
		"keys":     {SenderKeyDistributionMessage: &waE2E.SenderKeyDistributionMessage{}},
		"edit-ish": {ProtocolMessage: &waE2E.ProtocolMessage{Type: waE2E.ProtocolMessage_MESSAGE_EDIT.Enum()}},
	}
	for name, m := range drops {
		if _, ok := ToIncoming(info("x", false), m); ok {
			t.Fatalf("%s should be dropped", name)
		}
	}
	status := info("s", false)
	status.Chat = types.StatusBroadcastJID
	if _, ok := ToIncoming(status, &waE2E.Message{Conversation: proto.String("hi")}); ok {
		t.Fatal("status updates should be dropped")
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
