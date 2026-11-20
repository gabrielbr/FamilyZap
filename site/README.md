# Oiê website (oie.app)

The landing page: one static page, no build step, no tracking.

- `index.html`: the page, Portuguese first. `assets/site.js` swaps it to English (the PT/EN switch, or `?lang=en`) and runs the hero's family line.
- `assets/site.css`: the page's styles. Colours come from `assets/tokens.css`, a **copy** of `design/generated/tokens.css`; copy it again when the tokens change (never edit it here). The hero uses the logo's three colours (`01 Brand/logo/README.md`).
- `assets/`: the logo files and icons, copied from `01 Brand/logo/`. The line icons are the app's own set (`gg_ic_*`), inlined as an SVG sprite.
- Nunito comes from Google Fonts.

**The hero:** Tito types his message on the tablet (letter by letter), it goes out from the parent's WhatsApp signed "Tito:", Vovó sees it, and her answer types in under it, with Tito's message kept on top. Both travel back to the tablet. One loop is about 25 seconds. It's labelled "Exemplo". It has a Pausar button, stops while off screen, and shows its end frame under "Remove animations" / reduced motion.

**Download:** every "Baixar" links to the latest release in `gabrielbr/oie-releases`.

**Before it goes live** (the domain isn't registered yet, STATUS.md):
- add `<link rel="canonical">`, an absolute `og:image` and the Organization `url` once `oie.app` exists;
- host `site/` at the root (any static host). Nothing here needs a server.

Design: the direction is in `.impeccable/surfaces/site-index-html.md`; the audit is in `audits/2026-11-20-landing-page.md`.
