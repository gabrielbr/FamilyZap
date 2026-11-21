# 0017 · The website counts visits, simply, with GoatCounter

- **Date:** 2026-11-21
- **Decided by:** the owner: "for the landing page can we have some simple rudimentary analytics? I don't want a full-fledged Google Analytics." They chose GoatCounter over Cloudflare Web Analytics and over no third party at all.
- **Scope:** the website only (`site/`). **The app is unchanged:** it counts nothing and sends nothing about the family. The product law (no analytics in the app) stands.
- **What is counted:** a visit to each page (the page and, if it came from another site, that site's address without its query), and taps on "Baixar o Oiê".
- **How:**
  - Our own small script, `site/assets/count.js`. No script is loaded from GoatCounter; the page only sends a count to it.
  - No cookies and nothing stored on the visitor's device. GoatCounter keeps no IP address and shows only totals.
  - Nothing is counted when the browser has "Do Not Track" or Global Privacy Control on, or anywhere but the published site.
  - The owner can stop counting their own visits on a browser with `?naocontar=1`.
- **Said plainly:** every page's footer says "Este site conta as visitas sem cookies e sem dados pessoais, com o GoatCounter. O app não conta nada."
- **Why GoatCounter:** it's open source, free for a site this size and cookieless, so it needs no consent banner. It answers the questions the owner has (how many visit, from where, how many tap Baixar) and nothing more. Google Analytics would collect far more than we need and contradict the site's tone.
- **Revisit if:** the site needs to say anything it can't count this way, or GoatCounter changes its terms.
