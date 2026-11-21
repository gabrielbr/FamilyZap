# Audit · website · the page for family and friends ("Como falar com uma criança no Oiê")

- **Date:** 2026-11-21
- **What changed:**
  - new static pages `site/falar-com-a-crianca/` (PT) and `site/talk-to-a-child/` (EN);
  - a few rules in `site/assets/read.css` (`.read-p`, `.say`, item titles under a step);
  - a footer link on the landing page (PT and EN);
  - `MessageRuleTest.theWebsitesExamplesHold`.
- **The owner's ask:** "a specific page … on how to start the engagement with a kid. So let's say that they want to invite my son to something, how they start it."
- **Skills used and how:** all four were loaded and read in this session. `ux-user-flows` wrote the flow (`docs/flows/website-talk-to-a-kid.md`, §13 review there). `impeccable` ran its craft floor and the audit dimensions, on screenshots at 375 and 1280 px, light and dark, PT and EN. `graphic-design-fundamentals` ran its §13 critique, and `less-is-more` ran its five questions.

## 1. ux-user-flows (§13)
The full review is in the flow. Every item passes except Evidence (🟡: one grandparent should read the page and send a first message).

## 2. impeccable (audit, Read mode)
| Dimension | Score 0–4 | Note |
|---|---|---|
| Accessibility | 4 | axe (WCAG 2.2 AA plus best practice): 0 violations at 320, 375, 768 and 1280 px, light and dark, both languages. Headings run in order (h1, h2 steps, h3 items). The two example lists are told apart by their titles, not colour. |
| Performance | 4 | Static, no script; the site's own font files. |
| Theming | 4 | Tokens only. Dark mode was checked. |
| Responsive | 4 | No overflow from 320 px, or at 200% text. The examples stack on a phone. |
| Anti-patterns | 4 | No cards as structure, no eyebrow, no emoji. The step numbers carry the order the reader follows. |
| **Total** | 20/20 | |

| ID | Severity | Finding | Fix | Status |
|---|---|---|---|---|
| T1 | P2 | Item titles (h3) took the safety page's section-title style (800, up to 36px) and competed with the steps. | A scoped rule: 700 at 20px, like the safety page's items. | ✅ fixed |
| T2 | P3 | "Aviso" twice in the quiet-hours line. | "um aviso conta quantas chegaram" | ✅ fixed |

## 3. graphic-design-fundamentals (§13)
- **Hierarchy ✅:** title, then the three numbered steps, then Bom saber. The squint test reads 1, 2, 3.
- **Contrast ✅:** "Chega" lines sit on the surface with a solid hairline; "Fica com o pai ou a mãe" lines are muted with a dashed one, and each list has its title. Muted on page passes 4.5:1, as on the safety page.
- **Typography ✅:** Nunito only; curly apostrophes in EN (fixed: 25 straight ones); 62ch measure.
- **Consistency ✅:** the safety page's rings, nav, footer and item rhythm.
- **Simplicity ✅:** see less-is-more.

## 4. less-is-more
| Element | Question (1–5) | Kept / moved / merged / removed | Where it went |
|---|---|---|---|
| "Baixar" in the nav | 4 (weak for this reader's goal) | Removed | The reader isn't installing anything; "Conheça o Oiê" and the logo lead to the landing page |
| "Em grupos" item | 2 (duplicate of step 2) | Merged | One sentence in step 2: "Em grupos, vale o mesmo." |
| The contents list (as on the safety page) | 3 | Not added | Three steps fit one scroll |
| A link in the landing page's nav | 5 (rare for a parent visitor) | Not added | Footer only; parents share the link |

| Dimension | 0–4 |
|---|---|
| Elements per screen | 4 |
| Duplicates left | 4 |
| Text to read when tired | 4 (three steps, each two or three lines) |
| Taps on the common actions | 4 (one link, then read) |

## 5. Copy
Informal "você", Portuguese first. No "!", no em dashes, no emoji. It says plainly that the parent sees everything, and it never promises a message got through. An invitation goes through the parents ("eles veem tudo também e podem combinar os detalhes"), with no meeting-up wording.

## Checks
- `MessageRuleTest` (with the page's examples): passes.
- axe, overflow and 200% text on both pages: clean.
- Full check before the PR: `python3 design/build.py --check && ./gradlew testDebugUnitTest verifyRoborazziDebug lintDebug assembleDebug`.

## Open items
- A grandparent reads it and sends a first message (Evidence).
