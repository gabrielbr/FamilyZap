# 0003 · Build the WhatsApp link on whatsmeow

- **Date:** 2026-11-18
- **Status:** approved
- **Approved by:** the owner, 2026-11-18, in the Claude session ("check whatsmeow so we can use it to implement"; follow wacli's rules)

## Context
Decision 0001 chose a linked-device bridge on the kid's tablet. We need a library that speaks
WhatsApp's linked-device (multi-device) protocol and can run inside an Android app. The owner
pointed at wacli (wacli.sh), a widely used CLI built on whatsmeow, for how it manages the
connection and the ban risk.

## Decision
- **Use whatsmeow** (Go, MPL-2.0), unmodified, compiled into the app with gomobile.
- **Follow rules L1–L9** in `docs/whatsapp-link.md`. They are wacli's practices plus Oiê's own:
  - honest device name;
  - only allowed chats;
  - human pace;
  - no duplicate sends;
  - bounded reconnects;
  - little history;
  - no fake activity;
  - the session stays on the tablet;
  - a ban stops everything.

## Options considered
| Option | For | Against |
|---|---|---|
| **whatsmeow (Go)** | The most used linked-device library; used by wacli and others; active (last commit 2026-11-16); events for bans and logouts; builds for Android (spike) | Go inside an Android app: about 7 MB more per architecture; no tagged releases; MPL-2.0 obligations |
| Baileys (TypeScript) | Popular | Needs a Node.js runtime on the tablet, much heavier and stranger on Android than a Go library |
| Our own protocol code | Full control | Months of work on a protocol that changes without notice |

## Consequences
- **No library removes the ban risk** (wacli says "use at your own risk"). The parent notice before linking stays.
- **APK size grows:** about 7 MB per architecture. Ship arm64 and armv7 only.
- **The build needs Go and the Android NDK** (CI too, once the link ships).
- **Each whatsmeow update is a pinned commit,** read before bumping, because WhatsApp changes the protocol without notice.
- **Licence:** Sobre › Licenças names whatsmeow (MPL-2.0) and where its source is.
