# Audit · landing page (site/) · 2026-11-20

- **Trigger:** the owner asked for the product's landing page, made with impeccable, graphic-design-fundamentals, web-design-skills and value-focused-landing-page. The hero had to animate how it works: the kid sends a message, it goes through the parent's phone, and it arrives at Grandma.
- **Owner's answers:**
  - main action: download the APK from the releases page;
  - language: PT-BR first, with an EN switch;
  - delivery: repo (`site/`) plus a private preview;
  - structure: "the family line", the transit-diagram route.
- **Skills (loaded and read this session):** value-focused-landing-page, web-design-skills, impeccable (new-work, craft-floor, the finish review), graphic-design-fundamentals §13, ux-user-flows §13, less-is-more.
- **Screens checked:** desktop 1440 and mobile 390, light and dark, PT and EN, the hero mid-story, reduced motion. No horizontal scroll at either width.

## value-focused-landing-page
- **Promise:** "A família ao alcance." The subline puts the parent's outcome first: the kid messages the family from the tablet, through the parent's WhatsApp, only to chosen people.
- **Demo:** the customer's story, labelled "Exemplo":
  - Tito writes;
  - it goes out from the parent's WhatsApp, signed;
  - Vovó sees it;
  - her answer comes back.
- **Objection answers:**
  - "A família não precisa baixar nada";
  - the ban risk, verbatim from the voice guide;
  - "Não é do WhatsApp".
- **Nothing that isn't live:**
  - calls are "Ainda não";
  - no price is stated (none is decided);
  - the domain isn't registered, so there's no canonical or absolute share image yet (site/README.md).
- **SEO:**
  - one h1, a title of 47 characters and a meta description of 129;
  - FAQPage and SoftwareApplication JSON-LD matching the visible FAQ.

## impeccable
- **Direction:** the owner locked a structure on the decision round. The direction contract is in `.impeccable/surfaces/site-index-html.md`, code-led with no image generation.
- **Finish review:** run by a fresh reviewer that didn't share this build's context.
  - **Round 1: fix.**
    - Pause under 48px.
    - A floating "Exemplo" tag that labelled nothing.
    - The hero buttons only scrolled the page.
    - The transit devices were unused.
    - Uneven section widths.
    - Check marks on "no" statements.
  - **Round 2: fix.** Five of the six fixes were resolved. Partial: terminus marks. New: one literal colour and a 12px tag.
  - **After round 2:**
    - the colour became a hero variable (`--brand-muted`);
    - the tag moved to 13px;
    - the end stations got heavier terminus rings.

  These three were recaptured and looked at, but not re-scored by the reviewer: the two-round budget was spent.
- **Detector:** two findings, both dismissed.
  - `cramped-padding` on the FAQ list: the summary rows are 56px tall.
  - `aphoristic-cadence`: it read labels as one sentence.
- **Native-style audit (web):**

| Dimension | Score |
|---|---|
| Accessibility | 4: one `role="img"` summary for the animation; Pausar (aria-pressed); it pauses off screen; the end frame under reduced motion; skip link; labelled language group; 48px targets |
| Performance | 4: one page, no framework, no images except the logo; the timers stop off screen |
| Theming | 4: tokens from `design/generated/`; light and dark; the hero keeps the logo's coral in both |
| Responsive | 4: 390 to 1440 with no overflow; the plate is clamped inside the route |
| Anti-patterns | 4: no device mockup, no feature-card row, no eyebrow labels, no gradient text |

## graphic-design-fundamentals (§13)
- **Hierarchy:** the headline, then the line, then the button. One idea per section.
- **Contrast:**
  - ink on coral about 4.7:1 (passes for text, just);
  - muted on cream 6.3:1;
  - graphics at 3:1 or better.
- **Grid:** a 1200px page; the spine aligns with Tito's station; spacing from the 4–64 scale only.
- **Type:** Nunito only; display scale at line-height 0.98; body measure 62ch.
- **Colour:** coral owns the hero field. On the cream page the coral is the spine, the station rings and the final download, and nothing else.
- **Real size:** checked at 390px and 1.0× at 1440.

## ux-user-flows (§13)
- **The visitor's flow:**
  1. understand (the hero);
  2. believe (the controls, what the family sees, honesty);
  3. act (Baixar, with four install steps).
- Every "Baixar" goes straight to the releases page. "Como instalar" jumps to the steps.
- **Branches:** reduced motion; no JavaScript (all content shows, the line stays still); English; dark.
- **Ethics:** the risk is said before the download, and plainly. There are no safety claims.

## less-is-more
| Element | Q | Decision |
|---|---|---|
| Pricing section | 1 | Not added: nothing is decided |
| Testimonials | 1 | Not added: there are none yet, and inventing them is out |
| A phone or tablet mockup | 4 | Not added: the brand bans devices as heroes; the line shows the mechanism |
| A second hero CTA ("Fale com a gente") | 2 | Not added: one action |
| Separate "how it works" section | 2 | Not added: the hero is the how-it-works |

**Result:** no open P0, P1 or P2. Open for the owner: register oie.app, then add the canonical and an absolute share image.
