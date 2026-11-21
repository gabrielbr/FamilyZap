import numpy as np
from PIL import Image, ImageFilter
rng = np.random.default_rng(7)
import sys
out = (sys.argv[1] if len(sys.argv) > 1 else '.') + '/'

def smooth_noise(h, w, scale, octaves=4):
    acc = np.zeros((h, w)); amp = 1; tot = 0
    for o in range(octaves):
        sh, sw = max(2, int(h / scale * 2**o)), max(2, int(w / scale * 2**o))
        n = rng.random((sh, sw))
        im = Image.fromarray((n * 255).astype(np.uint8)).resize((w, h), Image.BICUBIC)
        acc += np.asarray(im, float) / 255 * amp; tot += amp; amp *= 0.5
    return acc / tot

# 1. Film grain tile (overlay): mid-grey noise with alpha.
g = rng.normal(0.5, 0.2, (160, 160)).clip(0, 1)
Image.fromarray((g * 255).astype(np.uint8), 'L').save(out + 'grain.png', optimize=True)

# 2. Iridescent foil: hue follows a warped diagonal, pastel, with crinkle.
H, W = 900, 1600
y, x = np.mgrid[0:H, 0:W] / np.array([H, W])[:, None, None]
warp = smooth_noise(H, W, 900, 2)
crink = smooth_noise(H, W, 14, 2)
t = (x * 0.7 + y * 0.45 + warp * 0.9 + crink * 0.06) % 1.0
hsv = np.dstack([t, np.full_like(t, 0.30) + crink * 0.08, 0.95 + 0.05 * crink])
rgb = np.asarray(Image.fromarray((hsv * 255).astype(np.uint8), 'HSV').convert('RGB'), float)
spec = (np.clip((warp - 0.66) * 5, 0, 1) ** 2)[..., None] * 255
rgb = np.clip(rgb * 0.85 + spec * 0.5, 0, 255).astype(np.uint8)
Image.fromarray(rgb).save(out + 'foil.webp', quality=78)

# 3. Liquid chrome for display type: sky, a hard horizon, warm ground, wavy and streaked.
H, W = 360, 1400
yy = np.linspace(0, 1, H)[:, None] + (smooth_noise(H, W, 420, 2) - 0.5) * 0.18
stops = [(0, (40, 70, 200)), (0.28, (190, 215, 255)), (0.46, (255, 255, 255)), (0.5, (25, 20, 45)),
         (0.58, (110, 75, 60)), (0.8, (235, 205, 170)), (1, (255, 248, 236))]
img = np.zeros((H, W, 3))
for c in range(3):
    img[..., c] = np.interp(yy, [s[0] for s in stops], [s[1][c] for s in stops])
streak = smooth_noise(H, W, 30, 2) * 0.6 + np.repeat(smooth_noise(H, 8, 6, 2)[:, :1], W, 1) * 0.4
img = np.clip(img + (streak[..., None] - 0.5) * 40, 0, 255).astype(np.uint8)
Image.fromarray(img).save(out + 'chrome.webp', quality=85)

# 4. Sparkles: four-point stars with a soft glow, on transparent, for the dark scenes.
S = 900
acc = np.zeros((S, S))
yy, xx = np.mgrid[0:S, 0:S]
for _ in range(16):
    cx, cy = rng.integers(40, S - 40, 2); r = rng.uniform(8, 34)
    dx, dy = np.abs(xx - cx) + 0.5, np.abs(yy - cy) + 0.5
    d = np.sqrt(dx**2 + dy**2); star = (np.clip(1 - (np.sqrt(dx * dy) / (r * 0.5)), 0, 1) ** 4) * np.exp(-d / (r * 1.6)) + np.exp(-(d**2 / (r * 0.3) ** 2))
    acc += star
for _ in range(140):
    cx, cy = rng.integers(0, S, 2); r = rng.uniform(0.8, 2.2)
    acc += np.exp(-(((xx - cx) ** 2 + (yy - cy) ** 2) / r**2)) * rng.uniform(0.3, 0.9)
acc = np.clip(acc, 0, 1)
rgba = np.dstack([np.full((S, S), 255), np.full((S, S), 250), np.full((S, S), 235), acc * 255]).astype(np.uint8)
Image.fromarray(rgba).save(out + 'sparkle.webp', lossless=False, quality=80)
print('ok')
