# Audit · website · the landing page's words tell the home-phone story

- **Date:** 2026-11-21
- **What changed:** `site/index.html` and `site/assets/site.js`, words only (PT and EN). `site.css` gained two lines: the footer credit's spacing, and long headline words may wrap. Decision 0014.
- **Skills used and how:** ux-user-flows §13, graphic-design-fundamentals §13 and less-is-more ("The test for every element" and "Output") were loaded and read in this session. impeccable was loaded earlier in this session; its audit dimensions are applied here to a copy-only change, with no new finish review, because the design (reviewed at 20/20 in `2026-11-20-website-polish-to-max.md`) is unchanged.
- **Checked by machine:**
  - axe at 320, 375, 768, 1024, 1280 and 1440, in PT and EN, light and dark: 0 violations;
  - no sideways scroll, also at 200% text;
  - "Remove animations" shows every step;
  - no script errors.
- **Looked at:** desktop and phone captures of the hero, the story, the highlights, the FAQ and the footer.

## 1. ux-user-flows (§13 flow review)
No flow change: the same entry, picker, six steps, FAQ and download. Only what each step says changed.

| # | Question | Pass? | Note |
|---|---|---|---|
| 1 | Job | ✅ | The parent's job is unchanged. The first screen now names the idea in one line people remember: the home phone. |
| 3 | Happy path | ✅ | Same length. The download is still on the first screen. |
| 9 | Memory | ✅ | The story still uses the family the visitor picked. |
| 10 | Exits | ✅ | Download, safety page, and now the code and the author's site. |
| 13 | Accessibility | ✅ | axe clean. The new headline wraps at 200% text on a 375px phone. |
| 14 | Ethics | ✅ | The honest line stays in the FAQ ("Eu vejo o que a criança manda? Vê."). The risk is still in the notice and the FAQ. |
| 15 | Evidence | 🟡 | The friend's "helicopter parent" reaction is the test: show them this version. |

## 2. impeccable (audit, copy-only change)
| Dimension | Score 0–4 | Note |
|---|---|---|
| Accessibility | 4 | axe clean in every theme and language; nothing overflows at 200%. |
| Performance | 4 | No new assets. |
| Theming | 4 | No colour changes. |
| Responsive | 4 | Checked at 320–1440. The long English headline now wraps. |
| Anti-patterns | 4 | No new elements. The footer credit is one plain line. |
| **Total** | 20/20 | |

| ID | Severity (P0–P3) | Finding | Fix | Status |
|---|---|---|---|---|
| C1 | P1 | "Remember the home phone?" overflowed by 68px at 200% text on a 375px phone. | `overflow-wrap: break-word; hyphens: auto` on the hero heading | ✅ fixed, 0px |

## 3. graphic-design-fundamentals (§13 critique)
| # | Item | Pass? | Note |
|---|---|---|---|
| 1 | Brief | ✅ | The owner's ask: the 90s home-phone content in v2's design and structure. |
| 2 | Concept | ✅ | One idea runs through the page: the home phone, the family line, the phone book, the meeting outside. |
| 3 | Hierarchy | ✅ | The question headline reads first, then the lede and the download. |
| 7 | Typography | ✅ | Same roles. The new headline balances on two lines on desktop. |
| 9 | Consistency | ✅ | The same words as the safety page's "Por que fizemos o Oiê". |
| 10 | Simplicity | ✅ | Highlights stay at three. |
| 11 | Real size | ✅ | Seen at 375 and 1280. |

## 4. less-is-more
| Element | Question (1–5) | Kept / moved / merged / removed | Where it went |
|---|---|---|---|
| Highlight "Você vê o que sai" | 4 (weak for the goal: it read as monitoring) | Removed from the highlights | Still answered in the FAQ, "Eu vejo o que a criança manda?" |
| "pausar uma conversa" in step 6 | 4 | Removed | It's in the app's parents' area. The step names contacts and quiet hours. |
| Footer credit (new) | 1 (the owner asked; tells who made it) | Added, one line | Footer, under the tagline |
| "O código no GitHub" (new) | 1 | Added | Footer links |

| Dimension | 0–4 |
|---|---|
| Elements per screen, compared with before | 4 (the same, plus one footer line) |
| Duplicates left | 4 |
| Text to read when tired | 4 (the step texts are the same length or shorter) |
| Taps on common actions (must not rise) | 4 (unchanged) |

## 5. Copy (anti-slop and the voice)
- No "!", no em dashes, no hype words.
- No "100% seguro".
- No WhatsApp logo or affiliation; the notice is unchanged.
- "o Oiê não tem servidor" kept.
- Buttons are unchanged and start with a verb.

## What was cut
Two looks tried on the branch before this were dropped at the owner's request: a 90s TV infomercial, then a Y2K restyle with rendered art. Neither shipped, and none of their files remain.

## Open items
- Show the page to the friend who gave the feedback (STATUS).
