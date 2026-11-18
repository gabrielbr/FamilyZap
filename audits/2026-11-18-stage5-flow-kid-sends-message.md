# Audit · Stage 5 · Flow: kid sends a message

- **Date:** 2026-11-18
- **What changed:**
  - `docs/flows/kid-sends-message.md` (a new flow);
  - icons switched to the design system's line set (`02 Design System/README.md` §9, `AGENTS.md`, `01 Brand/imagery-and-icons.md`).
- **Skills used and how:**
  - `ux-user-flows`: loaded and used to design the flow; its §13 review is below.
  - `less-is-more`: loaded; its flow and screen rules are below.
  - `graphic-design-fundamentals`: loaded earlier this session; only the icon change is visual, see below.
  - `impeccable`: **not run.** There are no screens yet, only the flow. It runs on the first screens of this flow.

## 1. ux-user-flows (§13 flow review)
The full table is in the flow file: 13 of 15 pass, and 2 are partial (12 Speed, 15 Evidence).
| Severity | Finding | Fix | Status |
|---|---|---|---|
| Important | "Can't send" in quiet hours may stop a bedtime good night | Decide a per-contact exception in the contacts and quiet hours flow | Open, for the owner |
| Polish | Record button label must follow its state for screen readers | Gravar → Gravando 0:12 → Solte para enviar | Open, for the screens |
| Polish | Real send time through the link is unknown | Measure in the first build | Open |

## 2. impeccable
Not run: there is no screen yet.

## 3. graphic-design-fundamentals
Only the icon rule changed. One set across all of the owner's apps passes #9 Consistency, and 22dp at stroke 1.75 inside a 48dp target passes #12. Nothing else visual changed.

## 4. less-is-more
| Element | Question | Decision | Where it went |
|---|---|---|---|
| "Enviada · time" on every sent message | 2 (duplicate) | Merged | Shown only under the latest sent message |
| Kid notice ("your parent can see") | 3 (understand once) | Moved | Shown once at first open; then in Ajuda |
| A button on the empty home | 1 (helps the next thing?) | Removed | The kid can't fix it; the line says who can |
| A confirm dialog before sending | Flow rule (undo, not confirm) | Removed | A 5 s Desfazer snackbar instead |
| Search or "new contact" on the kid side | 4 (goal) and product law | Not added | The kid only sees allowed family |

| Dimension | 0–4 |
|---|---|
| Elements per screen, compared with before | 4 (new flow, kept to photos and names on the home, one accent in the conversation) |
| Duplicates left | 4 |
| Text to read when tired | 4 (states are a few words) |
| Taps on the common actions (must not rise) | 4 (voice: 2 taps and a hold) |

## 5. Copy (anti-slop)
Checked against `01 Brand/voice.md`:
- Buttons start with a verb (Enviar, Desfazer, Cancelar, Trocar).
- No "!", no emoji, no tech words ("conectar de novo", never "session").
- Numbers carry their labels ("2 novas", "até 18h").

## What was cut
The per-message sent status, the confirm dialog, and the empty-state button.

## Open items
- Per-contact quiet hours exception (owner, in the contacts flow).
- The Oiê sticker set to draw.
- The 6 icons to add to the core.
