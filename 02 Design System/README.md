# Oiê Design System

_Stage 4. Every value lives in [`design/`](../design/README.md) (core + brand + product layers) and is generated into `design/generated/`; this file explains how to use it._

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
- **Scope:** Oiê is for talking with family: the people a parent chose. It is not social media, not a game and not a way to meet anyone new. It runs on the kid's tablet and stores nothing on our servers.
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
Proportion: about 60 page, 30 ink, at most 10 accent. Values and every contrast ratio are in [`design/brand/oie/brand.md`](../design/brand/oie/brand.md); the checks run with `python3 design/build.py --check`.

| Token | Light | Dark | Role | Contrast |
|---|---|---|---|---|
| page | `#FFF6EE` | `#1F1210` | page background | n/a |
| surface | `#FFFFFF` | `#2A1714` | cards | n/a |
| ink | `#2A1714` | `#FBEFE6` | text, icons | 16.0:1 / 16.1:1 on page |
| muted | `#6E5650` | `#C9B3AA` | secondary text | 6.3:1 / 9.1:1 |
| line | `#EFD9CC` | `#3D2823` | hairlines (decorative) | n/a |
| accent (core `mint`) | `#E8573C` | `#E8573C` | one per screen: the main action, the frame. Fill only | 3.4:1 / 5.1:1 (graphic) |
| on-accent (core `on-mint`) | `#2A1714` | `#1F1210` | text on accent | 4.8:1 / 5.1:1 |
| stop (core `coral`) | `#B23A2E` | `#FF8C78` | destructive fills, always with a word | on-coral 5.9:1 / 8.1:1 |
| coral-text | `#A12E24` | `#FF9A88` | errors as text | 6.7:1 / 8.9:1 |
| selected | `#FCE1D6` | `#4A2620` | active tab, picked chip or contact | ink on it 13.7:1 / 11.7:1 |

**The accent rule:** one filled coral element per screen. Picked things use `selected`, not coral. Colour never carries meaning alone: a word always goes with it.

## 5. Typography
**Nunito** is the only family (SIL OFL 1.1; 400, 600, 700, 800; Portuguese accents).

| Role | Size / line | Weight | Job |
|---|---|---|---|
| display | 30/36 | 800 | the greeting on the kid's home, one per screen |
| title | 22/28 | 700 | screen and sheet titles |
| label | 16/22 | 700 | buttons, contact names |
| body | 16/24 | 400 | messages, explanations |
| body-small | 14/22 | 400 | secondary lines |
| caption | 13/18 | 600 | times, small labels (nothing that carries meaning under 12sp) |

Line length 45–70 characters (`content-max` 600dp on the tablet).

## 6. Grid & space
- **Spacing (core):** 4, 8, 12, 16, 24, 32, 40, 48, 64. No other values.
- **Radii (core):** 4 controls, 8 cards, 16 pills and sheets, round for photos.
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
- function only, always with a text label on the kid side (one exception: the conversation's composer on a narrow screen, decision 0010);
- never emoji as icons.

## 10. Components
The rules are below. Previews come with the first screens (Stage 5), built from the tokens only.
- **Main button:** 52dp tall, coral fill, ink label, verb first. One per screen.
- **Quiet button:** 52dp, surface fill with a line border, ink label.
- **Contact tile (kid home):** 96dp round photo (or the initial on cream), name below in label. One tap opens the conversation. Only allowed contacts appear.
- **Message row:** the kid's messages on `selected`, family messages on `surface`, with ink text, times in caption and muted. Not green bubbles, and no WhatsApp look.
- **Record button:** round, coral, 72dp inside the conversation. Hold to record a voice message, release to send, slide away to cancel. It is the screen's one accent.
- **Notice sheet:** the parent's risk notice and the kid's first-use notice. Title, body and one main button. It appears once and is never dismissed by accident.
- **Chip:** 16dp radius, `selected` when picked.
- **Snackbar:** ink ground, page text, at most one action.

## 11. Production
- **Screen:** colours are sRGB HEX; logos and icons are SVG or Android vector drawables; the generated files are `design/generated/OTokens.kt` and `tokens.css`.
- **Fonts:** Nunito from Google Fonts (OFL). Bundle the 4 TTFs in the app; upload them to Canva or Figma for anyone editing social posts.
- **Print:** CMYK and Pantone values TBD before any print job; PDF/EPS logo masters still to make.
- **Files:** brand files in `01 Brand/`, deliveries in `04 Deliveries/` named `OIE_<Edition>_<Asset>_<Ratio>_<NN>.<ext>`.
