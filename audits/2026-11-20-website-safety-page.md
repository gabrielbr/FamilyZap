# Audit · website · the safety page (why Oiê, and a guide for parents)

- **Date:** 2026-11-20
- **Trigger:** the owner asked for two things:
  - research on keeping kids safe online and the tools parents can use (Google Family Link and others);
  - a sub-page that collects it as a knowledge center with sources, opening with why Oiê exists. Kids should reach friends the way we did with the home phone in the 90s, to meet in real life, without being in the hands of companies that show them ads or collect their data.
- **Process:**
  1. **Research:** a brief with sources, checked on 2026-11-20 (kept outside the repo). Before use, I re-checked the links here:
     - **dropped**, because they didn't load or couldn't be verified: the FTC releases (404 from here), NSPCC and Internet Matters (blocked), the "R$ 50 million" fine, the OpenDNS addresses, the PoPETs figure, and WhatsApp's parent-managed accounts (availability in Brazil not confirmed);
     - **spot-checked against the source text:** TIC Kids 2025 (68%, 30%), SBP's hours, the federal guide's under-12 line, the ECA Digital FAQ (in force 17 March 2026; no data collection for ads; tools for families), Disque 100 (24 h), SaferNet (free);
     - **Planalto** (the LGPD's text) can't be reached from this session; it's the law's official address.
  2. **Design:** flow `docs/flows/website-safety.md` (ux-user-flows, §13 review in it); impeccable surface brief `.impeccable/surfaces/site-seguranca-index-html.md` (mode: Read).
  3. **Build:** `site/seguranca/index.html`, `site/safety/index.html`, `site/assets/read.css`. The landing page's nav ("Segurança") and footer link to it, and switch to `safety/` in English.
  4. **Audit:** the four skills, loaded and read this session; screenshots at 375px and 1280px, light and dark, in both languages, plus 200% text.
  5. **Fix:** the findings below, then looked at again.
  6. **Docs:** CHANGELOG, STATUS, `site/README.md`.

## ux-user-flows (§13)
In the flow file. Two clicks from the landing page to a tool's setup page. Every outside link names where it goes ("Ajuda do Google", "(em inglês)"). The page works without scripts and prints with link addresses.

## impeccable · critique and audit (web, Read mode)
**Detector:**
- 12 "cramped padding" warnings: the contents list and the item lists are hairline-ruled rows whose text sits centred in 44–52px. That's the rule-list pattern, so I kept them.
- 2 "pulsing dot" warnings: the landing page's typing dots in the shared `site.css`, which aren't used on this page.

**axe (WCAG 2.2 AA + best practice):** clean on both pages, light and dark, at 375px and 1280px. The landing page is still clean with its new link.

| Finding | P | Fix |
|---|---|---|
| The three numbers rendered as squeezed columns: `.facts` already exists in the landing page's CSS. | **P1** | Renamed to `.figures`. |
| On a phone the h1 touched the header: the phone rule reset the reading column's top padding. | P2 | Only the side padding changes on a phone. |
| The numbers' rules had two widths, with too much space between items. | P3 | One measure, no extra gap. |
| Straight quotes in the copy. | P3 | Curly quotes. |
| "Telas fora do quarto" sat over the SBP's hours, which don't say that. | P2 (accuracy) | Retitled "Tempo de tela pela idade". The bedroom advice moved to the risk "Tela demais", sourced to the AAP, which says it. |

| Dimension | Score |
|---|---|
| Accessibility | 4: one h1, ordered headings, link text names the destination, 44px targets, axe clean, 200% text fits |
| Performance | 4: static HTML, no script; the CSS is shared with the landing page plus a 3 KB file |
| Theming | 4: tokens only, light and dark looked at |
| Responsive | 4: 375 to 1280 with no overflow; one reading column |
| Anti-patterns | 4: no icon-card grid, no eyebrow labels, no fear; the brief's calm leaflet |
| **Total** | **20/20** |

## graphic-design-fundamentals (§13)
- **Hierarchy:**
  - the h1 and the 90s lede, then the body;
  - two big numbers;
  - the guide's h2 and contents;
  - each section with its ring marker (the landing page's device), items in bold h3, sources small and muted.
- **Contrast:** checked by axe in both themes. The sources are `muted` on `page` (a checked pair).
- **Grid:** one 46rem column, the landing page's side margin and section rings; spacing from the 4–64 scale.
- **Type:** Nunito only; body measure ≤ 62ch; real quotes and the › in menu paths.
- **Colour:** coral only in the logo and the rings. No accent fill: there is no action to sell here.

## less-is-more
| Element | Q | Decision |
|---|---|---|
| The federal guide's fact under the numbers | 2 | **Removed:** it's in "O que diz a lei". |
| "Nenhuma ferramenta deixa uma criança 100% segura" in the footer | 2 | **Removed:** said once, at the top. |
| A full "Fontes" list at the end | 2 | Not added: each item links to its own source. |
| International regulators' fines (FTC) | 4 | Not added: Brazil's own law makes the point, and the links didn't load. |
| WhatsApp's parent-managed accounts | 1 | Not added: availability in Brazil not confirmed. |
| Router brands, OpenDNS addresses | 4 | Not added: one family DNS example is enough. |
| "Voltar ao início do guia" | 1 | Kept: the page is long on a phone. |

| Dimension | 0–4 |
|---|---|
| Elements per screen | 4 |
| Duplicates left | 4 |
| Text to read when tired | 3: it's a reading page; each item is three lines at most |
| Clicks to the common need (a tool's setup) | 4: two from the landing page |

**Result:** no open P0, P1 or P2.
- **Before each change of this page:** re-check its links and facts. Rules and menus change, and the page shows the date it was last checked.
- **Product law:** no "100% safe" claim, no endorsement implied by WhatsApp, Meta, Google, Apple, Samsung or the government, and the bridge notice is linked from the top.
