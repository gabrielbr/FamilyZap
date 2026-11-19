# The WhatsApp link

How Oiê connects the kid's tablet to the parent's WhatsApp (decision 0001: a linked device on the
tablet; decision 0003: built on whatsmeow). **Status:** 🟡 the link runs the read-only rule test
(v0.5, `docs/flows/rule-test.md`). Since v0.8 it also runs the kid's side, once the parent turns it on
(`AppState.realMode`, off by default). The kid's side then talks through `RealLink` as its
`WhatsAppLink`; until then it stays on `SampleLink`.

### v0.8 on the kid's side
- **Receiving:** `RealInbox.toMessage` comes first. It drops chats that aren't allowed, the parent's own messages, and the tablet's own sends echoing back (all tested). Then comes `Inbox.decide`, then the notification. Photos, stickers and voice that are kept are downloaded and sealed.
- **Sending:** text, photos, and stickers (as JPEG pictures) go through `SendPace`. Each send uses the message's own id. A send that fails while the link is down is retried once, on the next "connected" (L4); after that it's marked failed for the kid. Voice waits for real recording (v0.9).

## How it fits together
```
WhatsApp servers ⇄ whatsmeow (Go, link/) ⇄ gomobile AAR ⇄ WhatsmeowLink (Kotlin, : WhatsAppLink) ⇄ Inbox.decide ⇄ the app
```
- **`link/`:** a Go module that wraps whatsmeow. `link/build-aar.sh` builds `oielink.aar` with
  gomobile for arm64 and armv7. It's about 7 MB per architecture inside the APK.
- **Events go up as `(kind, json)`** to a Kotlin listener. Every message goes through
  `Inbox.decide` **before** anything is stored, so the product law holds at a single point.
- **The session** (keys and protocol state) is a SQLite file in the app's private storage.

## The rules (L1–L9)
WhatsApp can restrict or ban a number that uses an unofficial client, and nothing removes that
risk (decision 0001). These rules keep Oiê looking like what it is, one family typing, and stop a
bug from looking like a bot. They follow what wacli (wacli.sh, also built on whatsmeow) does.

| # | Rule | How |
|---|---|---|
| L1 | **Honest device name.** The parent sees "Oiê" in WhatsApp's Linked devices. | `store.SetOSInfo("Oiê", …)` |
| L2 | **Only allowed chats.** Sends go only to contacts the parent allowed, never to the parent's own number, never to a list. | `Inbox.decide`, the allowed list; no bulk send anywhere |
| L3 | **Human pace.** At least 2 s between sends, at most 30 in 10 minutes. The app waits rather than refuses. | `SendPace` (tested) |
| L4 | **No duplicates.** A send is retried at most once, with the same message ID, only after a reconnect. Other errors show "Não foi" with Tentar de novo for the kid. | whatsmeow `SendRequestExtra{ID}` |
| L5 | **Bounded reconnects.** whatsmeow reconnects on its own, waiting 2 s longer each time, and stops after 8 tries. After that, the app tries again when the network or the app comes back. | `AutoReconnectHook`; `LinkService` network callback (v0.7) |
| L6 | **Little history.** At pairing, ask for 1 day of history. Oiê shows nothing from before the link. | `HistorySyncConfig` limits |
| L7 | **No fake activity.** No automatic "online", "typing" or read receipts. Presence is sent only so WhatsApp knows the name. | no `SendChatPresence`, no `MarkRead` (open question: read receipts) |
| L8 | **The session stays on the tablet,** in the app's private no-backup storage, and is deleted when the parent unlinks. | `noBackupFilesDir`; `Unlink` deletes it |
| L9 | **A ban stops everything.** On `TemporaryBan` or `LoggedOut`, stop sending. Tell the parent in plain words, in the parent area. Never retry around it. | `events.TemporaryBan`, `events.LoggedOut` |

## Mapping whatsmeow to Oiê
| whatsmeow | Oiê |
|---|---|
| `GetQRChannel` + `Connect` | Setup step "Conectar": the QR the parent scans in WhatsApp › Linked devices |
| `PairPhone` | The "Usar código" option, if the QR is hard to scan |
| `events.Message` (text, image, audio PTT, sticker) | `Inbox.decide`, then shown or dropped |
| `ProtocolMessage` type `REVOKE` | "Mensagem apagada" |
| Quoted message (`ContextInfo.StanzaID`) | the message rule's "reply to the kid" |
| `SendMessage`, `Upload` (image, audio) | `SendTextWithID`, `SendImage`, `SendVoice` (PTT): the kid's sends, through `SendPace`, one id per message (L4) |
| Own chat (`IsSelfChat`, by number or LID) | Only stickers are taken from it, offered to the kid (v0.10, `docs/flows/family-stickers.md`) |
| `SendSticker` | A family sticker the kid saved, as a real WebP sticker (v0.10) |
| `Download` | `Link.Download(id)`: a received photo, voice note or sticker, as bytes the app seals at once (`Photos.keepBytes`). The last 200 received stay downloadable. |
| `events.Connected` / `Disconnected` / `LoggedOut` / `StreamReplaced` | Conexão state; "Peça para {nome} conectar de novo" |
| `events.TemporaryBan` (codes 101–106) | rule L9 |

## In the background (v0.7)
The link belongs to the app process (`OieApp`), not to a screen. While it should listen (today: the
rule test is on), `LinkService` keeps it alive as a foreground service of type `remoteMessaging`,
with a silent, minimum-priority notification ("O Oiê está conectado.") on its own channel. It
reconnects when the network comes back, stops on a ban, and stops when the test is deleted.
Android may still pause the network in deep sleep (Doze); messages then arrive when the tablet wakes.

## Size
The library adds about 9 MB per architecture. Since v0.6 each release has one APK per architecture
(about 21 MB) plus the universal one (about 40 MB) for manual installs and for copies older than
v0.6. Self-update picks the tablet's own (`UpdateRules.offer`).

## Licence
whatsmeow is **MPL-2.0**: its own files stay MPL-2.0, and Oiê's code can stay closed. When Oiê
ships with it, we must:
- say in Ajustes e ajuda › Sobre that it includes whatsmeow under MPL-2.0;
- link to its source, github.com/tulir/whatsmeow, at the exact commit (`link/go.mod`).

We don't modify whatsmeow; a change to its files would have to be published under MPL-2.0. Its
dependencies are BSD, MIT, Apache or ISC licensed, and a full list ships with the release.

## Spike results (2026-11-18)
- whatsmeow `v0.0.0-20261116172437-19d61352436f` has no tagged releases; each update is a pinned pseudo-version. It needs Go 1.26 or newer.
- SQLite through `modernc.org/sqlite` (pure Go) works with dialect `sqlite`, foreign keys on. A fresh store opens unpaired (`go test` in `link/`).
- `gomobile bind` with NDK r27c builds `oielink.aar`: 13.6 MB, with `libgojni.so` at 18.8 MB per architecture before APK compression.
- Not tried: pairing with a real phone, sending, receiving. Those need the owner's WhatsApp, and they wait for the message-rule test.

## Before it ships
1. The message rule is tested with real chats (STATUS.md).
2. `WhatsmeowLink` implements `WhatsAppLink`. Every rule above has a test, where a test is possible.
3. ~~The release workflow builds the AAR (Go and the NDK in CI)~~ (v0.5).
4. ~~Ajustes e ajuda › Sobre carries the whatsmeow notice~~ (v0.5).
5. A test on a spare WhatsApp number first, never the owner's main one.
