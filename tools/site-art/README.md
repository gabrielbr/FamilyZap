# The website's art (where every image comes from)

Every image on the landing page (`site/assets/img/`) is made here, by code, from nothing: no stock, no photos, no AI image generation, no other company's artwork. Decision 0015.

| Image | Made by | What it is |
|---|---|---|
| `hero-1600.webp`, `hero-800.webp` | `render.html?scene=hero` (three.js) | A translucent tangerine tablet with a handset and a lime curly cord. The screen is drawn in the render (Nunito, the app's home as an illustration). |
| `phone-900.webp`, `phone-450.webp` | `render.html?scene=phone` | Today's phone, covered in old-web pop-up windows. |
| `blob-chrome.webp`, `blob-chrome2.webp`, `blob-lime.webp` | `render.html?scene=blob-chrome` (`&seed=5`), `scene=blob-candy&seed=3` | Liquid-chrome and candy blobs. |
| `foil.webp`, `chrome.webp`, `sparkle.webp`, `grain.png` | `gen_textures.py` (NumPy + Pillow) | Holographic foil, liquid chrome for the big lettering, sparkles for the night scenes, film grain over the page. |

## Re-make them

You need Node with Playwright and Chromium, `three@0.170.0`, Python 3 with NumPy and Pillow, and the Nunito files from `site/assets/fonts/`.

```sh
cd tools/site-art
npm i three@0.170.0            # makes ./node_modules (not committed)
cp ../../site/assets/fonts/*.woff2 .
python3 -m http.server 8765 --bind 127.0.0.1 &
node shoot.js hero 3600 2600 hero2x.png
node shoot.js phone 1400 1600 phone.png
node shoot.js blob-chrome 1200 1200 blob-chrome.png
node shoot.js blob-chrome 1200 1200 blob-chrome2.png "&seed=5"
node shoot.js blob-candy 1200 1200 blob-lime.png "&seed=3"
python3 gen_textures.py .
python3 export.py ../../site/assets/img
```

Chromium renders WebGL with SwiftShader (no GPU needed). The noise uses a fixed seed, so the textures come out the same every time.

Don't commit the PNGs, the fonts copy or `node_modules` here; only the scripts.
