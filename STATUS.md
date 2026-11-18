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
- [ ] The real WhatsApp link (after the message rule test): choose the library, decision 0003.
- [x] Photo sending and saving on the tablet (encrypted), 2026-11-18.
- [x] Sticker images, the quiet-hours editor, Ajuda e sobre, encrypted photo files, English (v0.2, 2026-11-18).
- [x] Self-update and the release workflow; portrait screenshots (v0.3, 2026-11-18).
- [ ] **Owner:** create the signing key and add the 4 `OIE_KEYSTORE_*` secrets; create the public repo `gabrielbr/oie-releases` and the `OIE_RELEASES_TOKEN` secret (docs/release.md).
- [x] Update sheet only in the parent area (owner, 2026-11-18); recorded as divergence D-14 in the design system.
- [ ] A native English reader check of `Strings.kt`.
