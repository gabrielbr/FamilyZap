# Audit · website · simple visit counts (GoatCounter)

- **Date:** 2026-11-21
- **The owner:** "for the landing page can we have some simple rudimentary analytics? I don't want a full-fledged Google Analytics." They chose GoatCounter (decision 0017).
- **What changed:**
  - **The script:** `site/assets/count.js`, on all five pages. It sends the page, the other site that linked here (no query) and taps on the two "Baixar o Oiê" buttons.
  - **Footers:** one line on every page in PT and EN: "Este site conta as visitas sem cookies e sem dados pessoais, com o GoatCounter. O app não conta nada."
  - **Docs:** decision 0017, `site/README.md`, CHANGELOG, STATUS.
- **Skills used and how:** the four skills were loaded and read in this session.
  - ux-user-flows: the visitor's path is unchanged. The one new path is the owner's opt-out (`?naocontar=1`).
  - The others: run on the footers of the landing page (EN, 390px) and the safety page (PT, 390px), captured with Playwright.
- **Test:** `count-test.js` (Playwright, in the session scratchpad). The published host was served from local files, and GoatCounter's endpoint was intercepted.
  - **Counted:** the home page with a referrer (sent as `https://l.instagram.com/`, with the query dropped); a tap on Baixar (event `baixar`); `?lang=en`; `seguranca/`; `talk-to-a-child/`.
  - **Not counted:** after `?naocontar=1`, then on another page; with Do Not Track on; on any other host.
  - No page errors.

## 1. ux-user-flows (§13)
- **Control ✅:** visitors whose browser asks not to be tracked aren't counted. The owner can stop counting their own visits.
- **Ethics ✅:** said plainly in every footer, and the app's claims stay true (the FAQ and the safety page speak about the app).
- **Evidence 🟡:** the first week's numbers on GoatCounter.

## 2. impeccable (audit)
| Dimension | Score 0–4 | Note |
|---|---|---|
| Accessibility | 4 | The footer line is plain text, read in order, the same style as the legal line. |
| Performance | 4 | About 2 KB, deferred, from our own host. One beacon per page; nothing blocks rendering. |
| Theming | 4 | Inherits the footer's muted text in light and dark. |
| Responsive | 4 | Wraps cleanly at 390px. |
| Anti-patterns | 4 | No banner, no pop-up, no cookie wall. |
| **Total** | 20/20 | |

| ID | Severity | Finding | Fix | Status |
|---|---|---|---|---|
| W1 | P1 | The first selector counted any link to the latest release, so the footer's "whatsmeow file" link would have counted as a download. | Count only links marked `data-count="baixar"`. | ✅ fixed |
| W2 | P2 | The site's README said "no analytics". | Reworded; the visit counts section was added. | ✅ fixed |

## 3. graphic-design-fundamentals (§13)
- **Hierarchy ✅:** the line sits with the other fine print, below the links.
- **Consistency ✅:** the same words in PT and EN on all five pages.

## 4. less-is-more
| Element | Question (1–5) | Kept / moved / merged / removed | Where it went |
|---|---|---|---|
| Page views and referrers | Helps now (1) | Kept | — |
| Tap on Baixar | Helps now (1) | Kept | — |
| Screen size, language picker and scroll depth events | 5 (rare need) | Not added | — |
| A cookie or consent banner | 2 (not needed: no cookies) | Not added | — |
| GoatCounter's own script | 2 (duplicate of 40 lines of ours) | Not added | `count.js` |

## Open items
- The owner's GoatCounter site is `ogabriel` (given 2026-11-21). They gave GoatCounter's standard snippet; we kept our own script instead, so no third-party code runs on the site. It sends the same counts to the same endpoint.
- Check the first visits show on ogabriel.goatcounter.com after the site publishes.
