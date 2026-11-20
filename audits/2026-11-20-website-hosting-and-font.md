# Audit · website · published from oie-releases, the font served by the site

- **Date:** 2026-11-20
- **Trigger:** the owner said: "move the site to oie-releases and self-host the font".
- **Changed:**
  - **Publishing:** `.github/workflows/site.yml` now pushes `site/` to `gabrielbr/oie-releases` (branch `gh-pages`) with `OIE_RELEASES_TOKEN`, and asks GitHub to serve it. It replaces GitHub Actions Pages in this repo, which was never turned on.
  - **The font:** Nunito 400, 600, 700 and 800 as woff2 in `site/assets/fonts/`. They're subset to Latin and punctuation from the app's own TTFs, about 14 KB each, with the OFL licence beside them.
    - Google Fonts' `<link>` and `preconnect` are gone from the three pages.
    - 400 and 800 are preloaded.
  - **A guard in the workflow:** no font, script or stylesheet may come from another site.

## ux-user-flows
No flow changes. The site's address becomes https://gabrielbr.github.io/oie-releases/; every in-site link is relative, so nothing breaks.

## impeccable (audit)
- **Coverage:** every character used on the three pages is in the subset (checked against the font's cmap: none missing).
- **No outside requests:** Playwright logged every request on the three pages. None leave the site, and Nunito 400/600/700/800 load.
- **Look:** the landing page and the safety page look the same as with Google Fonts (screenshots at 1280px).
- **Performance:** four ~14 KB files instead of Google's CSS plus font files from two other hosts; `font-display: swap`; 400 and 800 preloaded.
- **Score:** unchanged, 20/20 on the pages audited today.

## graphic-design-fundamentals
Same family, same weights, same rendering: one family, no change to type, colour or layout.

## less-is-more
| Element | Q | Decision |
|---|---|---|
| Google Fonts' two `preconnect` lines and its stylesheet | 1 | **Removed**: not needed any more. |
| Fonts for Cyrillic, Greek or Vietnamese | 4 | Not shipped: the site is PT and EN. |

**Result:** no open P0, P1 or P2. To confirm after merge: the `site` workflow's run, and the page loading at https://gabrielbr.github.io/oie-releases/.
