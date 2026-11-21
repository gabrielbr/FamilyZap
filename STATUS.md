# Status: Oiê (OIE)

Track: B · product with a team · Started: 2026-11-18

| Stage | Gate | Date | Notes |
|---|---|---|---|
| 1 Brief & sources | ✅ | 2026-11-18 | Brief, sources, PRODUCT.md; bridge decided (0001). |
| 2 Strategy & name | ✅ | 2026-11-18 | Strategy in `02 Design System/README.md` §1; name Oiê (0002). |
| 3 Identity | ✅ | 2026-11-18 | Logo C in coral, logo pack, voice, imagery and icons. |
| 4 Design system | ✅ | 2026-11-18 | design/ (core 0.31.1 + brand + layer), README, AGENTS.md brand rules, CI check. Component previews come with Stage 5. |
| 5 Screens & assets | ✅ | 2026-11-18 | All 4 v1 flows written; tablet prototype (8 screens, light/dark, portrait). Every screen uses only tokens, the core icons and the logo files. Remaining states are open items. |
| 6 Audit | ✅ | 2026-11-18 | `audits/2026-11-18-stage6-v0.1-prototype.md`: five skills; no open P0/P1; the message rule is decided and must be tested before the build. |
| 7 Deliver & keep alive | 🟡 | 2026-11-18 | App v0.1 (Compose, sample data, no real WhatsApp link) with photo sending and encrypted saving on the tablet; 30 tests; CI. Not released. |

**Legend:** ✅ gate passed · 🟡 in progress · ⬜ not started

## Approvals
| What | Who | Date | Where (message, doc) |
|---|---|---|---|
| Bridge route: linked device on the kid's tablet, no calls in v1 (0001) | Owner | 2026-11-18 | Claude session |
| Stage 1 gate passed | Owner | 2026-11-18 | Claude session |
| Strategy: one idea, tagline, positioning, personality, never-implies, scope | Owner | 2026-11-18 | Claude session |
| Name Oiê, app id `app.oie`, domain oie.app, code OIE; Oi_e risk accepted (0002) | Owner | 2026-11-18 | Claude session |
| Logo direction C · Close (the "oi" symbol risk accepted) | Owner | 2026-11-18 | Claude session |
| Logo colour coral #E8573C | Owner | 2026-11-18 | Claude session |
| Voice, risk notice and kid notice wording | Owner | 2026-11-18 | Claude session |
| Imagery (photos + logo shapes) and icons (rounded line, bold) | Owner | 2026-11-18 | Claude session |
| Stage 3 gate passed | Owner | 2026-11-18 | Claude session |
| Build on the Gabriel Design System core (like Cochilo); light + dark; Nunito; ink on coral | Owner | 2026-11-18 | Claude session |
| Full palette (design/brand/oie/brand.md) | Owner | 2026-11-18 | Claude session |
| No fairy mode in Oiê (it would bring pink back) | Owner | 2026-11-18 | Claude session |
| Icons: Phosphor Bold (replaced the same day, see below) | Owner | 2026-11-18 | Claude session |
| Stage 4 gate passed (previews deferred to Stage 5) | Owner | 2026-11-18 | Claude session |
| Message rule: replies to the kid, messages starting with the kid's name, and 24 h after the kid's last message (to test first) | Owner | 2026-11-18 | Claude session |
| PIN: 1-minute wait after 5 wrong tries; parent area closes after 2 min idle; groups allowed with a warning; quiet-hours presets Escola 7h–12h, Dormir 21h–7h | Owner | 2026-11-18 | Claude session |
| Stage 5 and Stage 6 gates passed | Owner | 2026-11-18 | Claude session |
| Update sheet only in the parent area; releases in a separate public repo `gabrielbr/oie-releases`; "Pedir para adicionar" yes; the six sticker words | Owner | 2026-11-18 | Claude session |
| Icons follow the core line set; the 6 missing icons are added to the core | Owner | 2026-11-18 | Claude session |
| Flow "Kid sends a message": can't send in quiet hours; camera and gallery; an Oiê sticker set; 5 s undo | Owner | 2026-11-18 | Claude session |
| Per-contact exceptions to quiet hours | Owner | 2026-11-18 | Claude session |
| Parent area on the tablet behind a 6-digit Oiê PIN (no parent app, no server); setup asks names, first contacts, quiet hours | Owner | 2026-11-18 | Claude session |
| v0.33 batch: no "escrevendo…"; "Preciso de você" only to marked people; the nudge off by default | Owner | 2026-11-20 | Claude session |

## Open items
- [x] Keywords: Warm, Simple, Trustworthy, Close (2026-11-18).
- [x] References: Headspace, Duolingo (2026-11-18).
- [x] Research spike done (2026-11-18): `00 Brief/decisions/0001-bridge.md`. WhatsApp now has parent-managed accounts for under-13s in Brazil.
- [x] Route decided (0001): linked-device bridge on the kid's tablet; no calls in v1.
- [ ] Check whether Google Play accepts the app, or plan direct-APK distribution (before Stage 7).
- [ ] Parent home screen goal (Stage 5).
- [x] Moved into the GitHub repo gabrielbr/familyzap (2026-11-18).
- [ ] Register oie.app (owner).
- [ ] Rename the GitHub repo to `gabrielbr/oie` (owner); then update the git remote.
- [ ] Check Instagram and TikTok handles for Oiê (owner).
- [ ] Search INPI (classes 9 and 38) for Oiê before the first public release (owner).
- [x] Oiê's row added to the design system (its 0.31.2, PR #12).
- [ ] Add camera, image, sticker, send, link and clock icons to the design system's core set.
- [x] Oiê sticker set drawn (proposed, 6).
- [ ] Component previews with the first screens (Stage 5).
- [x] Message rule decided (2026-11-18).
- [ ] **Test the message rule with 3 families' real chats before the build** (riskiest decision in v1).
- [x] Defaults confirmed (PIN wait, idle lock, presets, groups).
- [x] "Pedir para adicionar" approved and built (owner, 2026-11-18).
- [ ] Still proposed, no owner preference yet: no saving of received photos, no read receipts.
- [x] Remaining states drawn (screens 09–21, 2026-11-18). Ajuda/About still to draw (I-7).
- [x] Sticker words approved: Oiê!, Saudade, Boa noite, Te amo, Tô chegando, Obrigado (owner, 2026-11-18).
- [x] App v0.1 built: the UI shell with sample data (2026-11-18).
- [ ] Check v0.1 on a real tablet: install `app-debug.apk` from the CI run, with TalkBack and large text.
- [x] The real WhatsApp link: whatsmeow (decision 0003), in use since v0.8.
- [x] Photo sending and saving on the tablet (encrypted), 2026-11-18.
- [x] Sticker images, the quiet-hours editor, Ajuda e sobre, encrypted photo files, English (v0.2, 2026-11-18).
- [x] Self-update and the release workflow; portrait screenshots (v0.3, 2026-11-18).
- [ ] **Owner:** create the signing key and add the 4 `OIE_KEYSTORE_*` secrets; create the public repo `gabrielbr/oie-releases` and the `OIE_RELEASES_TOKEN` secret (docs/release.md).
- [x] Update sheet only in the parent area (owner, 2026-11-18); recorded as divergence D-14 in the design system.
- [ ] A native English reader check of `Strings.kt`.
- [x] Receiving: inbox rule, notifications, quiet-hours summary, delete for everyone (v0.4, 2026-11-18).
- [x] Website: the landing page in `site/`, PT and EN, with the animated family line (2026-11-20). Published from `gabrielbr/oie-releases` (gh-pages) at https://gabrielbr.github.io/oie-releases/; the font is self-hosted (2026-11-20). Open: register oie.app and point it there.
- [x] Website: the safety page (why Oiê + a sourced guide for parents), PT and EN (2026-11-20). Re-check its sources whenever it changes.
- [x] Website: the landing page's words tell the home-phone story, design unchanged (2026-11-21, decision 0014). Open: show it to the friend who gave the feedback.
- [x] v0.38: the kid's home as a list (phone) or cards with Escrever and Gravar áudio (tablet), each with the last message and when; Anexar replaces Foto and Figurinha in the chat (owner, 2026-11-20). Open: try Gravar áudio's tap-to-send with the kid.
- [x] v0.39: the kid's name on every kind of message: a voice note or a family sticker gets the name on its own line first, once per 2-minute burst; reactions stay bare (owner, 2026-11-21). Open: check on the device.
- [x] v0.40: the message rule: inside the 24 h window, talk about the kid ("o Tito já chegou?") and anything after the parent answers stay private (owner, 2026-11-21). Open: watch real family chats for misses either way.
- [x] v0.41: the low-risk pass: pace 4 s and 20 per 10 min; the unread mark on the parent's line is a switch, off by default; whatsmeow checked current (owner, 2026-11-21). Open: re-check whatsmeow monthly.
- [x] v0.42: groups back in Adicionar (the link keeps the groups list); the kid's Arrumar (favorites, own order); "Conversa só de {kid}" for people and groups (owner, 2026-11-21). Open: try dragging in Arrumar on the tablet; check groups show after a reconnect.
- [x] v0.43: commands from the parent's own chat: contatos, parar/voltar/remover/só/normal by number or name, adicionar + contact card (10 min), silêncio/liberar (owner, 2026-11-21). Open: try a real shared card on the phone.
- [x] v0.44: the parent area split by job: Ajustes (Conexão first, the Nomes row with its own screen, Bolha, Lembrete, Novidades) and Ajuda (questions, Problemas, Sobre); flow `docs/flows/parent-area-structure.md` (owner, 2026-11-21). Open: watch the owner find "apelidos", "Novidades" and "versão".
- [x] v0.45: news first on the kid home (newest first, back to place once read), brand-coral edge and "N novas" tag (decision 0015); phone pass of every screen (owner, 2026-11-21). Open: Tito notices the new card.
- [x] v0.46: groups as groups: everything in an allowed group, sender names, "Fora dos contatos", the parent's own group messages; react to any media by long press (decision 0016, owner, 2026-11-21). Open: try the cousins' group with one outsider.
- [x] v0.37: the kid posts a status (photo, video up to 1 min, text), only to the people in Contatos, through one modified whatsmeow file (decision 0003 update); off until the parent turns it on (2026-11-20). Open: the owner checks on a device that only Contatos sees it.
- [x] v0.36: Novidades: the kid watches, reacts to and answers the family's statuses (allowed people only; off until the parent turns it on; views show as the parent) (2026-11-20). Open: the owner checks on a device that WhatsApp shows the reaction on the status.
- [x] v0.35: the line to the parent marks itself unread after each message from the kid (2026-11-20). Open: the owner checks the unread dot on the phone.
- [x] v0.34: full design pass (all screens, four skills + anti-slop, four review rounds to max); forgot-PIN made safe (2026-11-20). Open: the owner confirms decision 0012 (wordmark size in the bar).
- [x] v0.33: ten family features: receipts, reactions, read aloud, listen before sending, drawing, stickers from photos, photos per person, birthdays, "Preciso de você", the gentle nudge (2026-11-20). Open: the owner's device test of receipts and reactions on real WhatsApp.
- [x] v0.32: voice messages re-packed into standard Ogg Opus before sending, so WhatsApp plays them (2026-11-20). Open: the owner's device test.
- [x] v0.31: WhatsApp's Lottie stickers move (decision 0008); GIFs, stickers and pictures from the keyboard or paste (2026-11-20).
- [x] v0.30: the kid can write to the parent who owns the WhatsApp, through a one-person group (fallback: the parent's own chat), decision 0007 (2026-11-19). Open: the owner's test that WhatsApp accepts the group.
- [x] v0.29: a bubble per active chat, opening only that chat with the keyboard up; roomier composer on narrow screens (2026-11-19).
- [x] v0.28: the kid sends videos (up to 3 min, shrunk to 720p with Media3, decision 0006) and GIF files from the gallery (2026-11-19). Open: the owner's device test.
- [x] v0.27: animated stickers move; videos and GIFs from family play in the chat (2026-11-19). Next: sending them (v0.28).
- [x] v0.26: allowed contacts' WhatsApp profile pictures, checked at most daily, sealed on the tablet (2026-11-19).
- [x] v0.25: Oiê in an Android bubble: a small window over other apps, turned on in Ajustes e ajuda (2026-11-19). Open: the owner's device test.
- [x] v0.24: "Conectando…" for the first 30 s after an update or a drop, instead of a false "desconectado" (2026-11-19).
- [x] v0.23: photos work (refused since v0.1 by a size-check mistake) and come upright; WhatsApp's storage no longer fails for want of a temp folder (issue #34, 2026-11-19).
- [x] v0.22: the kid's name goes out in WhatsApp bold, after an emoji the parent can choose (2026-11-19).
- [x] v0.21: "Oi Tito" counts; the kid's nicknames; commands from the parent's WhatsApp ("/Tito parar"), answered in the parent's own chat (decision 0005, 2026-11-19).
- [x] v0.20: the false "desconectado" fixed: coming back to Oiê no longer breaks the status of a working link (2026-11-19).
- [x] v0.19: the parent names each contact for the kid ("Vovó"), and sees each contact's number in Contatos and setup (decision 0004, 2026-11-19).
- [x] v0.18: "Enviar para o GitHub" opens a filled-in issue in the private FamilyZap repo; the owner submits it (2026-11-19).
- [x] v0.17: contacts status after linking (syncing, slow, empty), live list, people first; setup scrolls (2026-11-19).
- [x] v0.16: the cause of the crashes and logouts, found from the owner's report, is fixed: a "null" contact list right after linking, and Android refusing a background restart of the link service (2026-11-19).
- [x] v0.15: the report also catches Go fatal errors and Android exit reasons (2026-11-19). The owner's report decides the real fix for the logouts.
- [x] v0.14: problem report (on the tablet, copied by the parent); crash safety in the link; re-link after a logout fixed; photos sent with size and preview (2026-11-19). Waiting for the owner's next photo test and report.
- [x] v0.13: full design audit of every screen with the four skills (16/20 native audit, was 12/20); phone width fixed; phone and large-text screenshots added (2026-11-19).
- [x] v0.12: test features removed (sample, simulations, rule test); contacts reload as they arrive; add by number (2026-11-19). The owner tests the real link now.
- [x] v0.11: real voice messages, recorded and played (2026-11-19).
- [x] v0.10: family stickers. The parent sends a sticker to their own chat; the kid keeps it and sends it as a real sticker (2026-11-19).
- [x] v0.9: names editable in Ajustes e ajuda; the kid's messages are signed "Tito: …" on the real WhatsApp (2026-11-19).
- [x] v0.8: the kid's side can run on the real WhatsApp, once the parent turns it on in Conexão (off by default); real QR in setup (2026-11-19). Open: voice recording and playback (v0.9).
- [x] v0.7: the link keeps listening in the background, reconnects when the network returns (2026-11-19).
- [x] v0.6: media through the link (receive and send, tested in Go); per-architecture APKs (2026-11-19).
- [x] v0.5: the rule test with the real WhatsApp, read-only (docs/flows/rule-test.md, 2026-11-19). Next: the owner runs it for a day or two on real chats.
- [x] The WhatsApp link: whatsmeow chosen (decision 0003), Android build tried, rules L1–L9 written (2026-11-18). Waiting on the message-rule test.
- [x] Releases publish themselves when a version bump is merged to `main` (2026-11-18). Waiting on the owner's one-time setup in `docs/release.md`.
