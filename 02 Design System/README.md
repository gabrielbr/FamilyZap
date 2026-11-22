# Oiê Design System

_Stage 4. Every value lives in [`design/`](../design/README.md) (core + brand + product layers) and is generated into `design/generated/`; this file explains how to use it._

**Since v0.52 (2026-11-22, [decision 0018](../00%20Brief/decisions/0018-material-3-expressive.md)) the app wears Material 3 Expressive:** Google's components, colour scheme, type scale, shapes and motion, with Oiê's coral as the seed, Nunito as the family and the coral frame round every screen. Sections 4, 5, 6 and 10 say how. The logo, the voice and the graphic device are unchanged. The website's drawn tablet and its buttons and cards follow the app since 2026-11-22.

## 1. Character
_Stage 2 strategy, approved by the owner on 2026-11-18._

- **The one idea:** Family, within reach.
- **Tagline:** Send love home.
- **Positioning:** For families with kids aged 9–12, Oiê lets the kid message the people they love from a tablet, through a parent's WhatsApp, with only the contacts the parent chose. Unlike giving a kid their own phone or account, the family keeps talking where it already does. It is not a monitoring app and not made by WhatsApp.
- **Personality (this, not that):**
  - Warm, not sugary: kind and close, never cute-talk or babyish.
  - Simple, not childish: big and clear for a 9–12 kid, without treating them as small.
  - Honest, not alarming: tells parents the truth, including the ban risk, calmly.
  - Trusting, not watching: parents set the circle; it never feels like spying on the kid.
- **Never implies:**
  - that it's a WhatsApp or Meta product (no green bubbles, no WhatsApp logo or shapes);
  - that a child is "100% safe", or that it replaces a parent's attention;
  - that the WhatsApp link is official or risk-free for the parent's number;
  - that it's a way to watch, track or read up on the kid.
- **Scope:** Oiê is for talking with family: the people a parent chose. It is not social media, not a game and not a way to meet anyone new. It runs on the kid's tablet and has no servers of its own: it links to the parent's WhatsApp the way WhatsApp Web does, with no analytics, ads or tracking.
- **Name:** Oiê, said "oy-YEH" ([decision 0002](../00%20Brief/decisions/0002-name.md)). Never written like the telecom Oi's "Oi_e".
- **Three words:** warm · simple · trustworthy (and close).

## 2. Voice
Full rules in [`01 Brand/voice.md`](../01%20Brand/voice.md).
- **Three words:** warm, playful, plain. **Rule:** say it like you'd say it at the table.
- One voice with two registers: lighter for the kid, calm and explaining for the parent.
- Informal "você"; Portuguese is written first.
- No "!" except in "Oiê!"; no emoji in UI text; no em dashes; buttons start with a verb.
- Banned: hype words, Zap/Wpp, tech words in the UI, safety claims.

## 3. Logo
Files and rules in [`01 Brand/logo/README.md`](../01%20Brand/logo/README.md):
- **Files:** the wordmark "oiê" (coral, white, black); the "oi" symbol with a small-size cut; the app icon (cream on coral); favicons (16/32/48, ICO, SVG, 180, 192, 512, maskable).
- **Clear space:** the diameter of the i's dot.
- **Minimum sizes:** wordmark 64 px; symbol 16 px.
- **Don'ts:** no recolouring (and no green, yellow or pink), no stretching, no bubbles, hearts, houses or faces, never without the circumflex.

## 4. Colour
**v0.52:** Material's colour scheme, generated from the brand coral `#E8573C` by Material's own algorithm (the Fidelity scheme, which keeps the seed's colour; 2025 spec) in `design/m3/scheme.py`, into `ui/theme/OieScheme.kt`. Never edit the generated file. Dynamic colour (from the wallpaper) stays off, so the brand holds. The script checks every text pair at 4.5:1 and graphics at 3:1 in both themes.

| Material role | Light | Dark | Used for |
|---|---|---|---|
| surface | `#FFF8F6` | `#1D100E` | the page |
| surface-container (low, -, high, highest) | `#FFF0EE` … `#F7DDD8` | `#261816` … `#41312E` | cards, rows, bubbles, the input |
| on-surface / on-surface-variant | `#261816` / `#59413C` | `#F7DDD8` / `#E1BFB8` | text, icons / secondary text |
| primary / on-primary | `#AC2C16` / `#FFFFFF` | `#FFB4A5` / `#650A00` | the one filled action, section titles, the badge |
| primary-container | `#CE452C` | `#F56044` | the record button (FAB), faces on rows with news |
| secondary-container | `#FFA08D` | `#763225` | picked things, rows and cards with news, the kid's own bubbles |
| error / error-container | `#BA1A1A` / `#FFDAD6` | `#FFB4AB` / `#93000A` | stop and errors, always with a word |
| outline / outline-variant | `#8D706B` / `#E1BFB8` | `#A88A84` / `#59413C` | outlined buttons, dividers |

- **The frame** keeps the brand coral `#E8573C` (the core rule), as do the QR code's ground and the drawing pad.
- **The accent rule, Material's way:** one filled primary button per screen. News is the soft secondary container with one solid badge. Colour never carries meaning alone: a word always goes with it.

## 5. Typography
**Nunito** is the only family (SIL OFL 1.1; 400, 600, 700, 800; Portuguese accents). **v0.52:** it fills Material 3 Expressive's type scale (sizes, line heights and weights are Material's). Nunito has no 500, so Material's medium weight uses the 600 file.

| Oiê role | Material style | Size / line | Job |
|---|---|---|---|
| display | display small, emphasized | 36/44 | the greeting on the kid's home, one per screen |
| title | title large, emphasized | 22/28 | screen and sheet titles, names on cards |
| label | title medium | 16/24 | contact names, labels |
| body | body large | 16/24 | messages, explanations |
| body-small | body medium | 14/20 | secondary lines |
| caption | body small | 12/16 | times, small labels (never less than 12sp) |

Buttons, chips, the navigation bar and the badge use Material's own label styles. Line length 45–70 characters (`content-max` 600dp on the tablet).

## 6. Grid & space
- **Spacing (core):** 4, 8, 12, 16, 24, 32, 40, 48, 64. No other values.
- **Shapes (v0.52):** Material's shape scale: extra-small 4, small 8, medium 12 (cards), large 16 (bubbles, tiles), extra-large 28 (sheets), full (buttons). Every face wears Material's 9-sided cookie shape.
- **Motion (v0.52):** Material's expressive motion scheme (springs, with a little bounce on spatial moves); with "Remove animations" on, nothing moves.
- **App:** 16dp side margins on the phone and 24 on the tablet; one column up to 600dp.
- **Social:** 4:5 posts on a 6-column grid with 64 px margins.
- **Web:** 12 columns, max width 1200 px. The website (`site/`, since 2026-11-20) is the coral hero field carrying **the family line**: an ink line with cream station rings (the touching rounds) from the kid to the parent's WhatsApp to the family, which continues down the page as a coral spine with each section as a station. Its message is the app's message row on cream, never a bubble. **v2 (decision 0013):** the website tells that story with silhouettes (built from the touching rounds) holding the devices, and draws the parent's and the receiver's phones as WhatsApp screens in WhatsApp-like colours. That is the one place green and bubbles appear, and it's for the website only; the kid's tablet always shows Oiê's own screen.

## 7. Graphic device
**Touching rounds**, taken from the logo's "Close" letters: the o's ring, the i's dot and stem, the ê's hat, set so they touch each other. Coral and cream only. Use them in empty states, the splash, social backgrounds and the website hero. At most one cluster per screen, never behind text you must read, and never made into a face, a heart, a bubble or a house.

## 8. Imagery
Full rules in [`01 Brand/imagery-and-icons.md`](../01%20Brand/imagery-and-icons.md):
- **In the app:** the family's own contact photos only.
- **In marketing:** candid, warm real moments.
- **Banned:** stock families, identifiable kids' faces in marketing, devices as heroes, fear imagery.

## 9. Icons
**The Gabriel Design System's line set** (the core rule): 22dp, stroke 1.75, in a 48dp target.
- Files: the design system's `assets/icons/android/gg_ic_*.xml`.
- Camera, image, sticker, send, link and clock are missing from the set. They are being added to the core in the same style (owner's decision, 2026-11-18), not drawn here.
- Colour: ink, or muted for secondary; coral only on the one main action.
- function only, always with a text label on the kid side (one exception: the conversation's composer on a narrow screen, decision 0010, where Anexar shows its plus alone);
- never emoji as icons.

## 10. Components
**v0.52:** Material 3 Expressive's components, in `ui/common/Components.kt` and the screens.
- **Main button:** Material's filled button, medium size (56dp), verb first, the shape that squares a little when pressed. One per screen.
- **Quiet button:** Material's outlined button (56dp); with no word, the outlined icon button (56dp on the tablet, 40dp with a 48dp target on a phone).
- **Text button:** Material's (Arrumar, Esqueci o PIN, Remover), in primary.
- **Person row (Lista):** Material's segmented list item: each section is one group, big outer corners, small gaps. Face, name, the last message, the time and the "N novas" badge. A row with news is the secondary container with bold text.
- **Person card (Mosaico, Fotos):** the container colour, large shape, no border; news is the secondary container with the badge.
- **View choice (Arrumar):** Material's connected button group (Lista, Fotos, Mosaico); the picked one is filled, round and checked.
- **Message:** the kid's on the secondary container, family on the high surface container, large shape; one person's run joins up with small corners where their bubbles meet. The sender's name in primary; "Fora dos contatos" in an outlined tag. "Novas" is a small chip in the middle.
- **Input:** a pill on the high container, a 2dp primary edge while typing.
- **Record button:** Material's medium FAB look: primary container, rounded square with its shadow, round while recording. It is the conversation's one filled thing.
- **Parent area navigation:** Material's navigation rail on the tablet, its navigation bar on a phone.
- **Sheet:** Material's bottom sheet look: extra-large top corners, the drag handle, its container colour.
- **Switch, time picker, outlined text field, snackbar, badge:** Material's, in their own colours.
- **Not green bubbles, and no WhatsApp look** (unchanged).

## 11. Production
- **Screen:** colours are sRGB HEX; logos and icons are SVG or Android vector drawables; the generated files are `design/generated/OTokens.kt` and `tokens.css`.
- **Fonts:** Nunito from Google Fonts (OFL). Bundle the 4 TTFs in the app; upload them to Canva or Figma for anyone editing social posts.
- **Print:** CMYK and Pantone values TBD before any print job; PDF/EPS logo masters still to make.
- **Files:** brand files in `01 Brand/`, deliveries in `04 Deliveries/` named `OIE_<Edition>_<Asset>_<Ratio>_<NN>.<ext>`.
