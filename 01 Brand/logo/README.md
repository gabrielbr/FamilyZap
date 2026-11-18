# Oiê logo

Direction **C · Close**, approved by the owner on 2026-11-18 (see `../directions/README.md`).
Bold, round lowercase letters that touch: the letters hold on to each other, the way the family
does. The **ê's circumflex** is what turns "oi" into Oiê; never drop it from the wordmark.

## Files
| File | Use it for |
|---|---|
| `oie-wordmark-coral.svg` | The logo. Default on cream, white or light grounds, and on the dark ink ground |
| `oie-wordmark-white.svg` | On coral, on photos with a calm dark area, on the dark ink ground when coral isn't wanted |
| `oie-wordmark-black.svg` | One-colour print, fax-grade, engraving |
| `oie-wordmark-mono-e8573c.svg` | Same as coral, generated from the black master |
| `oie-wordmark-*-1200.png` | Where SVG isn't accepted |
| `oie-symbol*.svg` | The "oi" symbol alone: app icon, avatar, favicon. Only where the wordmark is already nearby or the space is square |
| `oie-symbol-small.svg` | The small-size cut (bigger counter, wider dot gap) for 24 px and below; `favicon.*` uses it |
| `oie-app-icon.svg`, `icon-192.png`, `icon-512.png`, `maskable-512.png`, `apple-touch-icon.png` | App icon: cream "oi" on a coral tile |
| `favicon.ico`, `favicon-16/32/48.png`, `favicon.svg`, `site.webmanifest`, `head-snippet.html` | Website icons; paste `head-snippet.html` into the page head |

Masters are `oie-wordmark.svg` and `oie-symbol.svg` (black, filled outlines; no live text, no strokes).

## Colour
| Name | HEX | Use |
|---|---|---|
| Coral | `#E8573C` | The logo. 3.6:1 on white, 3.4:1 on cream, 4.8:1 on ink: passes for a graphic (3:1), **not** for small text |
| Ink | `#2A1714` | Dark ground |
| Cream | `#FFF6EE` | Light ground, and the mark on the coral app icon |

These are the logo's colours only. The full palette, with roles and contrast for every pair, is
set in Stage 4 (`02 Design System/`). RGB, CMYK and Pantone values: TBD (Stage 4, production).

## Clear space and minimum sizes
- **Clear space:** the diameter of the i's dot on every side. Nothing else enters it.
- **Wordmark minimum:** 64 px wide on screen, 15 mm in print.
- **Symbol minimum:** 16 px, using the small-size cut below 24 px.

## Don'ts
- Don't drop or move the circumflex, or the i's dot.
- Don't write the name in a typeface instead of using the files. In text, write **Oiê** (with the circumflex), or Oie only where accents are impossible.
- Don't recolour (no green, no yellow, no pink: those belong to WhatsApp and to the telecom Oi), stretch, outline, rotate, or add shadows or gradients.
- Don't put it in a speech bubble, a heart or a house, or add a face to the "o".
- Don't place it on busy photos or patterns.
- Don't use the "oi" symbol alone where it could be read as the telecom Oi's brand (e.g. next to telecom or internet services); prefer the full wordmark.

## Tests run (2026-11-18)
Rendered and checked on cream, white, coral and ink; the wordmark at 64/48/32/24/16 px and the app
icon at 64/32/16 px; one colour; reversed. `svg_audit.py`: no live text, rasters, filters or strokes
in the masters. Not done: a print proof, and a trademark search (see decision 0002).
