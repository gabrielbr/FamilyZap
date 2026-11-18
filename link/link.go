// Package oielink is Oiê's link to the parent's WhatsApp: whatsmeow, wrapped for gomobile.
//
// Status: a feasibility spike (docs/whatsapp-link.md). It builds for Android and opens its session
// store; it is not wired into the app. The app keeps using SampleLink until the message rule is
// tested with real chats (STATUS.md). The rules L1–L9 in docs/whatsapp-link.md apply to all of it.
//
// whatsmeow is MPL-2.0: we use it unmodified and say where its source is (docs/whatsapp-link.md).
package oielink

import (
	"context"
	"encoding/json"

	"go.mau.fi/whatsmeow"
	"go.mau.fi/whatsmeow/store"
	"go.mau.fi/whatsmeow/store/sqlstore"
	"go.mau.fi/whatsmeow/types/events"
	waLog "go.mau.fi/whatsmeow/util/log"
	_ "modernc.org/sqlite"
)

// MaxReconnects is how many automatic reconnects whatsmeow tries before it stops and waits for
// the app (rule L5). Its own wait grows 2 s per try.
const MaxReconnects = 8

// Listener receives events as (kind, json). Kotlin implements it.
type Listener interface{ OnEvent(kind string, payload string) }

type Link struct {
	c *whatsmeow.Client
	l Listener
}

func init() {
	// L1: the parent sees "Oiê" in WhatsApp's Linked devices, so it's never a mystery device.
	store.SetOSInfo("Oiê", [3]uint32{0, 4, 0})
	// L6: pull as little history as WhatsApp allows. Oiê needs none from before linking.
	one := uint32(1)
	store.DeviceProps.HistorySyncConfig.FullSyncDaysLimit = &one
	store.DeviceProps.HistorySyncConfig.RecentSyncDaysLimit = &one
}

// Open opens (or creates) the session store at dbPath. The app keeps that file in its private
// storage, inside the area it encrypts (rule L8).
func Open(dbPath string, l Listener) (*Link, error) {
	ctx := context.Background()
	container, err := sqlstore.New(ctx, "sqlite", "file:"+dbPath+"?_pragma=foreign_keys(1)&_pragma=journal_mode(WAL)", waLog.Noop)
	if err != nil {
		return nil, err
	}
	dev, err := container.GetFirstDevice(ctx)
	if err != nil {
		return nil, err
	}
	c := whatsmeow.NewClient(dev, waLog.Noop)
	// L5: bounded reconnects; after that the app reconnects when the network or the app comes back.
	c.AutoReconnectHook = func(error) bool { return c.AutoReconnectErrors < MaxReconnects }
	k := &Link{c: c, l: l}
	c.AddEventHandler(func(e interface{}) {
		switch v := e.(type) {
		case *events.Message:
			b, _ := json.Marshal(map[string]any{"chat": v.Info.Chat.String(), "fromMe": v.Info.IsFromMe, "text": v.Message.GetConversation()})
			l.OnEvent("message", string(b))
		case *events.Connected:
			l.OnEvent("connected", "{}")
		case *events.LoggedOut:
			l.OnEvent("loggedOut", "{}")
		case *events.TemporaryBan:
			// L9: stop everything and tell the parent plainly.
			b, _ := json.Marshal(map[string]any{"code": int(v.Code), "expiresInSeconds": int64(v.Expire.Seconds())})
			l.OnEvent("temporaryBan", string(b))
		}
	})
	return k, nil
}

// Paired says whether the tablet is already linked to a WhatsApp account.
func (k *Link) Paired() bool { return k.c.Store.ID != nil }
