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

## Owner's feedback, round 1 (2026-11-20)
- **"The animation can be slower; typing in the message rows of Tito and Grandma; make it clear Grandma is replying, keeping Tito's message on top."**
  - **Done:** the travel takes 1.8 s (it was 1.1), and a loop is about 25 s (it was about 14).
  - **Typing:** Tito's words appear letter by letter, with a caret, under "Tito escreve no tablet". Vovó's answer types in its own row (`selected` ground, "Vovó" label) under Tito's message, with "Vovó está respondendo…" while she types and "Vovó respondeu" after.
  - **The message rows:** they stay the app's message rows, never bubbles (brand rule).
  - **Layout:** the route reserves room for the tallest plate at every width and language, and lays out again when the font arrives or the size changes. Under reduced motion, the end frame is the full conversation, still.
- **Checked:** desktop 1440 and phone 390 at five moments of the story, plus reduced motion. No horizontal scroll.

## v2: a scrolling story with people (owner's feedback round 2, decision 0013)
- **Feedback (the owner):**
  - more human; a scrollytelling page;
  - vector silhouettes of mother or father, son or daughter, and a grandparent;
  - WhatsApp screens, in its colours with no branding, for the parent's phone (the bridge) and the receiver;
  - not always grandma: friends too.
- **Process:**
  - decision 0013 records the exceptions (green, bubbles and devices, website only);
  - the direction contract was rewritten (pinned by the owner's words);
  - built code-led;
  - checked at every step on desktop and phone, light and dark, PT and EN, with reduced motion;
  - one fresh finish review, then a fix batch, then a verdict pass.
- **Skills (loaded and read this session):** impeccable (new-work, craft-floor, finish review), graphic-design-fundamentals §13, ux-user-flows §13, less-is-more, value-focused-landing-page, web-design-skills.

### What the page does now
- **"Monte a sua família":** son or daughter, mother or father, and who they write to (Vó Rosa, Vô Zé, Tia Bia, Lucas the school friend). The names, silhouettes and messages follow the choice. It opens on Vô Zé.
- **Six steps on a pinned stage:**
  1. The kid writes; the words type out.
  2. It leaves the parent's WhatsApp, signed, with the ticks shown.
  3. It arrives.
  4. The receiver types a reply ("digitando…" on the parent's phone), and it comes back to the tablet.
  5. "Não é só …": the receiver cycles through the others.
  6. The parent holds the tablet, in the parents' area.
- **On a phone:** the stage pins above the text and shows one person at a time.
- **Copy:** "a família e os amigos que você escolher" everywhere. The FAQ's first questions are "Quem recebe…?" and "A criança pode falar com os amigos?". The footer says the WhatsApp screens are example drawings.

### Finish review, round 1: fix (no P0 or P1)
| Finding | P | Fix |
|---|---|---|
| The line and the labels were under the fold at 1440×900 | P2 | Shorter hero; the stage sits near the top until the hero scrolls away |
| Story text stayed faded without JS (about 2:1) | P2 | The fade only applies under `.js` |
| Pinned stage high, with empty space below, out of line with the text | P2 | `.is-pinned` centres the stage |
| Device bezels vanished in dark | P2 | A muted 2px edge in dark |
| Picker labels flashed blank while switching | P3 | Colour and border transition with the background |
| Step 5 list wrapped 3 + 1 | P3 | A 2×2 grid |
| "Vovó"/"Vovô" differed only by an accent; the page opened on grandma | P3 | "Vó Rosa" and "Vô Zé"; it opens on Vô Zé |
| "Ana" appeared twice in step 6 | P3 | The phones step back further in step 6 |

- **Detector:** cramped-padding (the device bezels, intended), nested-cards (bubbles in the drawn phones, intended), aphoristic-cadence (it read labels as a sentence). All dismissed by the reviewer.

### less-is-more
| Element | Q | Decision |
|---|---|---|
| A separate "how it works" section | 2 | Not added: the story is the how-it-works |
| The v1 kid-side, controls and family sections | 2 | Merged into steps 1, 3 and 6 |
| A play/pause control | 1 | Not needed: scrolling drives the story; only step 5's cycle and the typing move, and both stop under reduced motion |
| A picker for every family member | 4 | Only three choices; the fourth person (the receiver) cycles in step 5 |

### Verdict pass (v2)
- **Resolved:** 7 of the 8 fixes.
- **Partial: the picker transition.**
  - **Re-checked:** recaptured after the transition ends, the picker and the stage agree (`v2-d-dad-friend-0`).
  - **Cause:** the "picker shows Vô Zé while the stage shows Lucas" mismatch was the capture landing mid-transition, not a desync.
- **Fixed after the verdict pass:**
  - Lucas has one label, "Lucas, da escola", in the chip, step 5 and the stage;
  - the four "Para quem" chips sit two by two, so none wraps alone.

  These two were checked by me, not re-scored by the reviewer (the round budget was spent).
- **Result:** no open P0, P1 or P2.
