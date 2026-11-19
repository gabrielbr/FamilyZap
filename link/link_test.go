package oielink

import (
	"path/filepath"
	"testing"
	"time"

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
