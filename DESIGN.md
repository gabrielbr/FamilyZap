---
name: Oiê website
description: The family line, told with people. Static pages for parents, PT-BR first with EN.
colors:
  page: "#FFF6EE"
  surface: "#FFFFFF"
  ink: "#2A1714"
  muted: "#6E5650"
  line: "#EFD9CC"
  coral: "#E8573C"
  selected: "#FCE1D6"
  coral-soft: "#FBE3DC"
  page-dark: "#1F1210"
typography:
  display:
    fontFamily: "Nunito, system-ui, sans-serif"
    fontSize: "clamp(2.25rem, 6vw, 4rem)"
    fontWeight: 800
    lineHeight: 1.04
    letterSpacing: "-0.03em"
  section:
    fontFamily: "Nunito, system-ui, sans-serif"
    fontSize: "clamp(1.625rem, 3.6vw, 2.75rem)"
    fontWeight: 800
    lineHeight: 1.08
    letterSpacing: "-0.02em"
  item-title:
    fontFamily: "Nunito, system-ui, sans-serif"
    fontSize: "1.25rem"
    fontWeight: 700
    lineHeight: 1.3
  lede:
    fontFamily: "Nunito, system-ui, sans-serif"
    fontSize: "clamp(1.25rem, 2.4vw, 1.5rem)"
    fontWeight: 400
    lineHeight: 1.45
  body:
    fontFamily: "Nunito, system-ui, sans-serif"
    fontSize: "1.0625rem"
    fontWeight: 400
    lineHeight: 1.6
  small:
    fontFamily: "Nunito, system-ui, sans-serif"
    fontSize: "0.9375rem"
    fontWeight: 400
    lineHeight: 1.6
rounded:
  control: "4px"
  card: "8px"
  pill: "16px"
spacing:
  s1: "4px"
  s2: "8px"
  s3: "12px"
  s4: "16px"
  s6: "24px"
  s8: "32px"
  s10: "40px"
  s12: "48px"
  s16: "64px"
components:
  button-primary:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.page}"
    rounded: "{rounded.control}"
    height: "52px"
  chip-picked:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.page}"
    rounded: "{rounded.pill}"
    height: "44px"
  source-link:
    textColor: "{colors.muted}"
    typography: "{typography.small}"
    height: "44px"
---

# Oiê website

The pages in `site/`: the landing page (`index.html`, mode Persuade) and the safety page (`seguranca/`, EN `safety/`, mode Read). The app's design system is `02 Design System/README.md`, with values in `design/`. This file covers how the website applies them. Colours come from `site/assets/tokens.css`, a copy of `design/generated/tokens.css` that CI compares. Never edit it here.

## Overview
A cream page, ink type and one coral thread. The landing page tells the family line with people drawn from the logo's touching rounds (decision 0013): a kid with the tablet, the parent whose WhatsApp is the bridge, and the person they write to. The safety page is a calm reading column in the same world. The site asks no other site for anything: fonts, styles and scripts are served from `site/assets`, with no analytics and no cookies.

## Colors
- `page`, `surface`, `ink`, `muted` and `line` carry everything. `coral` is the brand: the hero band, the rings, the frame on the tablet drawing and the message dot. It is never a text colour on cream.
- WhatsApp-like greens appear only inside the drawn `.wa` phone screens (decision 0013), as named constants in `site.css`. They are never used outside a drawing.
- Dark theme: the same roles from `tokens.css`. The browser bar follows the page (`theme-color` per scheme).

## Typography
- **Family:** Nunito only, served as woff2 in `assets/fonts/` (400, 600, 700, 800).
- **Weights:** one weight per job.
  - 800: display and section titles, and the big numbers.
  - 700: item titles, buttons, inline emphasis and call links.
  - 600: the contents list and the picker's legends.
  - 400: body, lede and small print.
- **Small print** (sources, review date, captions) is 15px at 400 in `muted`. Nothing that carries meaning is under 12px, except the words inside the drawings: those are illustrations hidden from screen readers, and the step text says the same thing.
- **Measure:** body up to 62ch; the lede ends on the same edge.

## Layout
- **Landing page:** one story grid, with steps on the left and a pinned stage on the right.
  - **Phones:** the stage pins over 62% of the screen and shows one person at a time. While the family form is on screen, the stage scrolls with the page.
  - **The rest of the page:** stations on a coral spine (`.line` and `.stop`).
- **Safety page:** a single 46rem column. Sections are marked by the spine's rings: beside the title on desktop, above it on a phone.
- **Spacing:** the scale only (4–64).

## Elevation & Depth
No drop shadows. Depth is `surface` on `page` and hairlines. The drawn devices are flat ink bezels, and in dark mode they get a muted 2px edge so they keep their shape.

## Shapes
Radii are 4 (controls), 8 (cards), 16 (pills and chips) and round (rings, faces). Every person is built from the logo's circles.

## Components
- **Download button:** ink fill and page text, 52px tall. There is one on the landing page's first screen (the hero). The nav's download button is shown only on the safety page.
- **Family picker:** 44px pills in even columns; the picked one is ink-filled.
- **Section ring:** a 44px ring with a 6px coral stroke (32px and 5px on phones).
- **Source link:** small print, muted, underlined, 44px target. Citations only.
- **Call link** (`tel:`): ink, 700, a 2px underline, first in its item.
- **Motion:**
  - scene changes take 250ms and presses 150ms, all on one ease (`cubic-bezier(0.22, 1, 0.36, 1)`), with no bounce;
  - the message's trip along the wire takes 1.2s, the story's one longer motion (decision 0013);
  - the drawn gestures run on multiples of the same tokens: typing taps at 300ms, the WhatsApp dots and the caret at 1s, and the key flash at 75ms;
  - out-of-focus people fade, and their screens go grey and lighter. The screens are pictures hidden from screen readers, with their words repeated in the step text (WCAG 1.4.3's exemption for text in a picture).
  - nothing loops forever: the step-5 receivers go round once;
  - with "Remove animations" on, everything shows its end state.

## Do's and Don'ts
- **Do:**
  - tie every fact to its official source;
  - say the WhatsApp risk plainly ("pode restringir ou bloquear números");
  - say "o Oiê não tem servidor".
- **Don't:**
  - load anything from another site (the `site` workflow fails if a page does);
  - use eyebrow labels, cards of icons, emoji, "!" or em dashes;
  - claim "100% seguro";
  - show a WhatsApp logo or imply the app is affiliated with WhatsApp.
