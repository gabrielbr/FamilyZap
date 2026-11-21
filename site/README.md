# Oiê website (oie.app)

The landing page: static pages, no build step, no analytics, no tracking, no cookies.

**v2 (2026-11-20, decision 0013):** a scrolling story told with people.
- **Picking a family:** the visitor picks son or daughter (Tito or Lia), mother or father (Ana or Paulo), and who they write to: Vó Rosa, Vô Zé, Tia Bia or Lucas, a school friend. The whole story uses those names.
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
  - Since 2026-11-21 (the owner: visitors didn't see they had to scroll), the stage takes 54% of the screen instead of 62%. The next step's heading always shows below the one being read.
  - The count, a six-segment progress bar and "Próximo" sit at the stage's bottom edge, right above the words. "Próximo" lands each step with its heading 16px clear of the stage.
- **Reduced motion:** nothing types or cycles; each step shows its end state.

**The words (2026-11-21, decision 0014):** the page tells the story of the 90s home phone: "Lembra do telefone de casa?". The kid talks to familiar people, to share their day and miss each other less. Since the owner's note of 2026-11-21, the site never talks about meeting up. The design and steps above are unchanged. The footer credits the open-source project to Gabriel Lopes, with links to the code and gabriellopes.com.

**The safety page (2026-11-20, flow `docs/flows/website-safety.md`):** `seguranca/` (PT) and `safety/` (EN), static pages with no script. At the top, why Oiê exists (the home phone of the 90s, no ads, no servers of ours, no analytics or tracking). Below it, a guide for parents: talking first, the tools parents already have (Family Link and others), the risks at 9–12, Brazil's rules and where to get help. Every item links to its official source, and the page shows the date it was last checked; **re-check the links and facts whenever the page changes**. Styles in `assets/read.css`. The landing page links to it from the nav ("Segurança") and the footer.

**The page for family and friends (2026-11-21, flow `docs/flows/website-talk-to-a-kid.md`):** `falar-com-a-crianca/` (PT) and `talk-to-a-child/` (EN), static pages in the safety page's style (`read.css`). They're for the people on the other side, a link the parent sends: be on the list, start with the child's name (examples of what reaches the tablet and what stays with the parent), then just talk, and what's good to know. The examples are checked by `MessageRuleTest.theWebsitesExamplesHold`; **if `MessageRule` changes, change this page too.** The top menu of every page links to it ("Família e amigos", next to "Segurança"), and so does the landing page's footer. On a phone the menu takes its own row under the logo and the language switch.

**Files:**
- `index.html`: the page, Portuguese first, with English through the switch or `?lang=en`. The silhouettes and line icons are inline SVG symbols.
- `assets/site.js`:
  - the people and the story's words in both languages;
  - the scroll steps;
  - the typing;
  - the PT/EN switch.
- `assets/site.css`: colours from `assets/tokens.css`, a **copy** of `design/generated/tokens.css` (copy it again when the tokens change, and never edit it here). It also uses the logo's colours. The WhatsApp-like colours are named constants used only inside `.wa` screens, as decision 0013 allows. There is no WhatsApp logo or name mark anywhere.
- **Download:** every "Baixar" links to the latest release in `gabrielbr/oie-releases`.

**Published at https://gabrielbr.github.io/oie-releases/** (owner, 2026-11-20). The source stays here; `.github/workflows/site.yml` copies `site/` to the `gh-pages` branch of the public repo `gabrielbr/oie-releases`, which also holds the downloads.
- **When:** on every push to `main` that changes `site/` or the tokens.
- **How:** with the `OIE_RELEASES_TOKEN` secret, the same one the releases use. The branch holds one commit, replaced each time.
- **The workflow fails if:**
  - `assets/tokens.css` has drifted from `design/generated/tokens.css`;
  - a page loads a font, script or stylesheet from another site.
- **One-time setup in oie-releases,** if the workflow can't turn it on itself: Settings › Pages › Source: Deploy from a branch › `gh-pages`, `/ (root)`.

**Fonts:** Nunito is served from `assets/fonts/` (woff2, subset to Latin and punctuation from the app's own files, SIL OFL 1.1 in `fonts/OFL.txt`). The site asks no other site for anything: no Google Fonts, no analytics, no cookies.

**Before it goes live** (the domain isn't registered yet, STATUS.md):
- add `<link rel="canonical">`, an absolute `og:image` and the Organization `url` once `oie.app` exists;
- point oie.app at GitHub Pages in `gabrielbr/oie-releases` (Settings › Pages › Custom domain), or host `site/` anywhere static.

The direction is in `.impeccable/surfaces/site-index-html.md`, the audit in `audits/2026-11-20-landing-page.md`.
