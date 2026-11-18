package oielink

import (
	"path/filepath"
	"testing"

	"go.mau.fi/whatsmeow/store"
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
