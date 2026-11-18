# Audit · Stage 5 · Flow: parent setup + link

- **Date:** 2026-11-18
- **What changed:**
  - `docs/flows/parent-setup-link.md` (a new flow);
  - PRODUCT.md and the brief now say Oiê is one app on the tablet with a parent area;
  - one line in `kid-sends-message.md` (where the parent reconnects).
- **Skills used and how:**
  - `ux-user-flows`: loaded and used to design the flow; §13 is below.
  - `less-is-more`: loaded; its rules are below.
  - `graphic-design-fundamentals`: nothing visual changed.
  - `impeccable`: not run, because there are no screens yet.

## 1. ux-user-flows (§13)
The full table is in the flow file: 12 of 15 pass, and 3 are partial (3 Happy path length, 13 the QR as the only way to link, 15 Evidence).
| Severity | Finding | Fix | Status |
|---|---|---|---|
| Important | The QR is the only way to link (it needs the phone's camera) | Add "Conectar com código" if the linking method supports WhatsApp's phone-number code | Open, check in the build |
| Polish | The WhatsApp menu names on the phone change between versions | Keep the 4 steps in one place in the code | Open |
| Polish | Six setup steps | Contacts and quiet hours are skippable; kept by the owner's choice | Accepted |

## 2. impeccable
Not run: there are no screens yet.

## 3. graphic-design-fundamentals
Nothing visual changed.

## 4. less-is-more
| Element | Question | Decision | Where it went |
|---|---|---|---|
| A separate parent app on the phone | 4 (goal) and product law (no server) | Removed | Parent area on the tablet |
| A tutorial before setup | 3 (understand once) | Not added | The 4 phone steps sit on the link screen, where they're needed |
| The parent's name field | Defaults over questions | Prefilled from WhatsApp | One tap to change |
| A confirm on every step | Undo, not confirm | Not added | Only unlinking confirms (destructive) |
| Quiet hours in setup | 5 (rare) | Kept with **Pular** | Owner's choice; also in the parent area |

| Dimension | 0–4 |
|---|---|
| Elements per screen, compared with before | 4 |
| Duplicates left | 4 |
| Text to read when tired | 3 (the risk notice is long on purpose: honesty wins here) |
| Taps on the common actions (must not rise) | 4 |

## 5. Copy (anti-slop)
Checked against `voice.md`:
- The risk notice is in full and before linking (product law).
- No tech words; WhatsApp's own menu names are used only to guide the parent there.
- Buttons start with a verb; no "!".

## What was cut
The parent app on the phone, the sync between devices, a tutorial, and confirms on ordinary steps.
