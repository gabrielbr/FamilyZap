# Audit · website · the new look in the mock-ups, photos instead of initials

- **Date:** 2026-11-22
- **The owner:** "Now update the landing page with the new UI, including the mockups. Also make the first mockup with photos of contacts, not only initials."
- **What changed** (`site/index.html`, `site.css`, `site.js`):
  - **The drawn tablet in the story** now uses the app's Material 3 Expressive light scheme (`--m3-*`, from `OieScheme.kt`):
    - **home:** "Oiê, Tito", a "Pessoas" title, four tiles with drawn photos in the cookie shape;
    - **chat:** bubbles without borders, the kid's on the secondary container;
    - **input:** a pill, with the FAB-like mic;
    - **parents' area:** grouped rows and Material's switch.
  - **The photos:** four drawn portraits (Vó Rosa, Vô Zé, Tia Bia, Lucas) follow the visitor's picks. The bar's face shows the person in the chat.
  - **Site shapes:** pill buttons, 16px cards, 8px controls. The reading pages pick them up too.
  - **Copy:** "Pessoas" / "People".
  - **Docs:** `site/README.md`, decision 0018, the design system README, CHANGELOG.
- **Skills used and how:** the four skills were loaded and read in this session. They were run on the rendered page in Chromium:
  - desktop 1280 at steps 0, 1, 2, 4 and 6, and the download section;
  - phone 390 at the same steps;
  - the drawn tablet at 3× (home, chat, parents' area);
  - the two reading pages.

## 1. ux-user-flows (§13)
No step changes: the same six steps, the same picks. The first step now shows faces, as the story's words say ("Um toque na foto").

## 2. impeccable (audit and critique, web)
| Dimension | Score 0–4 | Note |
|---|---|---|
| Accessibility | 4 | The drawing is `aria-hidden`, as before, and the steps' words carry the story. Button labels are unchanged; the pill shape keeps 44px targets. |
| Performance | 4 | The portraits are inline SVG symbols (a few hundred bytes each); no image downloads. |
| Theming | 4 | The drawing is always light, as before. The site's own colours are unchanged in both themes. |
| Responsive | 4 | Checked at 390 and 1280; the tiles' names ellipsize. |
| Anti-patterns | 4 | No stock photos, no AI faces, no real people (the imagery rules). |
| **Total** | 20/20 | |

| ID | Severity | Finding | Fix | Status |
|---|---|---|---|---|
| W1 | P1 | The portraits rendered square: the cookie clip lived in the `display:none` sprite, which Chromium doesn't apply. | The clip moved to a zero-size SVG outside the sprite. | ✅ fixed, re-checked |

## 3. graphic-design-fundamentals (§13)
- **Hierarchy ✅:** in the drawn home, the greeting comes first, then the title, then the faces.
- **Colour ✅:** each portrait has its own soft ground, in the drawing pad's palette. Coral stays the frame and the mic.
- **Consistency ✅:** the same shapes as the app's screenshots (`81-kid-home-photos`).

## 4. less-is-more
| Element | Question (1–5) | Kept / moved / merged / removed | Where it went |
|---|---|---|---|
| Drawn photos | 1 (the owner asked; the story says "a tap on the photo") | Added | — |
| Real or AI photos of people | 5 (imagery rules: no stock families) | Not added | — |
| The view switch (Lista, Fotos, Mosaico) in the drawing | 2 (too small to read, not the story's point) | Not added | — |

## Checks
- Playwright on the local site: no page errors, desktop and phone.
- `python3 design/build.py --check`.
