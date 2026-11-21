# 0015 · The landing page in Y2K style

- **Date:** 2026-11-21 · **Status:** accepted · **By:** the owner
- **Replaces:** decision 0014's look (the TV infomercial), before it shipped. The story stays: the 90s home phone, told scene by scene, with the FAQ at the bottom and the open-source credit. The safety page is unchanged.

## Context
The infomercial draft felt flat and drawn. The owner: "Add textures to the design, avoid the vector look and feel … This is not looking good. Restart, but before research for Y2K graphics and style. Use it for that. Same concept of 90's and storytelling." The research is in `docs/research/y2k-style.md`.

## Decision
The landing page wears the look of 1999–2000: candy translucent plastic, liquid chrome, holographic foil, sparkles, film grain, gel buttons and old-web windows.
- **Its art is rendered, not drawn:**
  - 3D renders (three.js): the tangerine tablet-phone, today's phone with pop-ups, chrome and candy blobs;
  - textures generated in code.
- **Where each image comes from:** `tools/site-art/README.md`.

**Brand exceptions, approved by the owner for this page only** (they carry over from 0014, plus what Y2K needs):
- "!" in the playful lines;
- Nunito 900 and italics;
- a Y2K palette (tangerine, lime, blueberry night, silver) beside coral;
- gradients, gloss and soft shadows: the plastic and chrome are made of them;
- liquid-chrome lettering on the two big reveals ("Apresentamos o Oiê", the price);
- pill-shaped gel buttons.

The app and the safety page keep every brand rule. Spacing still comes from the scale, and motion is still 150/250 ms with no bounce, honouring "Remove animations".

## Kept, whatever the costume
- **Product law:**
  - the WhatsApp risk is said plainly and readably, next to the download button and in the notice;
  - no "100% safe";
  - no WhatsApp logo or affiliation;
  - "o Oiê não tem servidor".
- **No invented claims:** no testimonials and no fake numbers. "Grátis" and "código aberto" are true.
- **The pictures are illustrations:** the tablet's screen shows the app's home as a drawing, and the footer says so.
- **Delivery:** the page asks no other site for anything; the images are served from `site/assets/img`.
