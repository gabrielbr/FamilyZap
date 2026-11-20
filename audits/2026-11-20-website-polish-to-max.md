# Audit · website · impeccable polish of both pages to full marks

- **Date:** 2026-11-20
- **Trigger:** the owner asked for two things:
  - "Please use impeccable in the new segurança page. Text weights are weird. Use it until it reaches 100%, use polish."
  - "Also evaluate the homepage so it hits total."
- **Method:** impeccable `polish` and `typeset`, loaded and read this session, with `craft-floor`. An independent reviewer (a fresh agent each page, no edits) scored each round against `critique-rubric.md` (Nielsen 10 × 4) and the audit's 5 dimensions (× 4). It rendered every round with Playwright at 320–1280px, in PT and EN, light and dark, at 200% text and with reduced motion, and ran axe (WCAG 2.2 AA). I fixed findings between rounds and ran my own sweeps.

## Safety page (`site/seguranca/`, `site/safety/`)
| Round | Nielsen | Audit | Fixed after it |
|---|---|---|---|
| 1 | 35/40 | 17/20 | **Type roles:** one weight per job. Item titles 700 at 20px (were 800 at 18px), small print 400 at 15px muted (sources were 700 at 14px), contents 600, the honest line bold only on its first sentence. **Layout:** sources sit with their item; 190 and 100 are `tel:` links; on phones the ring sits above the title (full-width text); heading outline h2 › h3 › h4. **Details:** `theme-color` follows the theme; the lede ends on the body's edge; 700 preloaded. |
| 2 | 38/40 | 20/20 | Call links became actions (ink, 700, first in their item); the guide title is a clear step above the section titles on phones. |
| 3 | **40/40** | **20/20** | None left. |

## Landing page (`site/index.html`)
| Round | Nielsen | Audit | Fixed after it |
|---|---|---|---|
| 1 | 28/40 | 13/20 | **P1:** dimmed captions failed contrast; now only the drawing steps back. **Overflow:** the picker, tiles, others and nav wrap, with none from 320 to 1280 or at 200%. **Phone story:** the picker no longer sits under the stage, and the reply returns to the tablet. **Navigation:** "Ver a história" goes to step 1; a step counter; a replay link. **Motion:** on tokens, one ease, no bounce, nothing loops. **Copy:** "restringir ou bloquear"; SHA-256 explained; a FAQ on a blocked number. **Cleanup:** no drop shadow, one download in the first screen, DESIGN.md, the decision 0013 motion note. |
| 2 | 33/40 | 15/20 | **P1, introduced in round 1:** the centring shift ran on phones and pushed the caption over the words; now there's no shift on phones. **Step 1 on phones:** fixed by the reading line under the stage, the stage pinning as the form leaves, and the landing margins. **Smaller fixes:** the wire clips the dot instead of the stage (the hands were cut); inactive devices are solid and lighter (opacity had shown the person through the device); clear EN tile names; gesture timings are token multiples. |
| 3 | 36/40 | 19/20 | **Phones:** each step is as tall as the band, so the band is never empty. **Navigation:** "Próximo" on the counter. **Copy:** the hero note says to open the page on the kid's tablet; the install step says what to do if it's blocked. |
| 4 | **40/40** | **20/20** | None left. |

**Detector:** the remaining findings are false positives, as the reviewer confirmed:
- cramped padding on the device bezel;
- nested cards on the drawn bubbles;
- WhatsApp's own typing dots;
- concatenated labels.

**Exception recorded:** the drawn screens' words are part of a picture, hidden from screen readers and repeated in the step text (WCAG 1.4.3's exemption for text in a picture; DESIGN.md). axe is clean regardless.

## The other skills
- **ux-user-flows:** the landing flow gained "Ver a história" to step 1, "Próximo", replay, the blocked-number and blocked-install recovery, and the right-device note before download. The safety page added one-tap calls. Both flows end somewhere useful.
- **graphic-design-fundamentals (§13):**
  - one weight per role and the size steps hold on both pages;
  - grayscale works, since unread, active and focus never rely on colour alone;
  - spacing comes from the scale;
  - no drop shadows.
- **less-is-more:** these were not added:
  - a per-section "back to contents" link;
  - a step picker (the counter carries "Próximo" instead);
  - a second download button in the first screen (removed).

**Result:** both pages score 40/40 and 20/20, with no open P0–P3.
