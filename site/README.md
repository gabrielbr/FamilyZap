# Oiê website (oie.app)

The landing page: one static page, no build step, no tracking.

**v2 (2026-11-20, decision 0013):** a scrolling story told with people.
- **Picking a family:** the visitor picks son or daughter (Tito or Lia), mother or father (Ana or Paulo), and who they write to: Vovó, Vovô, Tia Bia or Lucas, a school friend. The whole story uses those names.
- **The stage:** it pins while the steps scroll past. It shows three silhouettes built from the logo's touching rounds, each holding a device:
  - the kid with the tablet (Oiê's own coral screen);
  - the parent with the phone (WhatsApp-like screen, the bridge);
  - the receiver with the phone (WhatsApp-like screen).
- **The steps:**
  1. The kid writes; the words type out.
  2. The message leaves the parent's WhatsApp, signed.
  3. It arrives.
  4. The receiver types a reply, which comes back to the tablet.
  5. It isn't only grandma: the receiver changes every few seconds.
  6. The parent holds the tablet, in the parents' area.
- **On a phone:** the stage pins over the top of the screen and shows one person at a time.
- **Reduced motion:** nothing types or cycles; each step shows its end state.

**Files:**
- `index.html`: the page, Portuguese first, with English through the switch or `?lang=en`. The silhouettes and line icons are inline SVG symbols.
- `assets/site.js`:
  - the people and the story's words in both languages;
  - the scroll steps;
  - the typing;
  - the PT/EN switch.
- `assets/site.css`: colours from `assets/tokens.css`, a **copy** of `design/generated/tokens.css` (copy it again when the tokens change, and never edit it here). It also uses the logo's colours. The WhatsApp-like colours are named constants used only inside `.wa` screens, as decision 0013 allows. There is no WhatsApp logo or name mark anywhere.
- **Download:** every "Baixar" links to the latest release in `gabrielbr/oie-releases`.

**Published on GitHub Pages** at https://gabrielbr.github.io/FamilyZap/ by `.github/workflows/site.yml`, on every push to `main` that changes `site/` or the tokens. The workflow fails if `assets/tokens.css` has drifted from `design/generated/tokens.css`. One-time setup by the owner: Settings › Pages › Build and deployment › Source: **GitHub Actions**.

**Before it goes live** (the domain isn't registered yet, STATUS.md):
- add `<link rel="canonical">`, an absolute `og:image` and the Organization `url` once `oie.app` exists;
- point oie.app at GitHub Pages (Settings › Pages › Custom domain), or host `site/` anywhere static.

The direction is in `.impeccable/surfaces/site-index-html.md`, the audit in `audits/2026-11-20-landing-page.md`.
