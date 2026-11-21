"""Trim the renders and write the website's images (site/assets/img).

Run from this folder after `node shoot.js` has written the PNGs (see README.md):
    python3 export.py ../../site/assets/img
"""
import shutil
import sys
from PIL import Image

out = (sys.argv[1] if len(sys.argv) > 1 else '../../site/assets/img') + '/'


def trim(path):
    im = Image.open(path)
    box = im.getchannel('A').point(lambda a: 255 if a > 6 else 0).getbbox()
    return im.crop(box)


hero = trim('hero2x.png')
for w in (1600, 800):
    x = hero.copy(); x.thumbnail((w, w * 2)); x.save(out + f'hero-{w}.webp', quality=82, method=6)
phone = trim('phone.png')
for w in (900, 450):
    x = phone.copy(); x.thumbnail((w, w * 3)); x.save(out + f'phone-{w}.webp', quality=82, method=6)
for n in ('blob-chrome', 'blob-chrome2', 'blob-lime'):
    x = trim(n + '.png'); x.thumbnail((440, 440)); x.save(out + n + '.webp', quality=82, method=6)
for n in ('grain.png', 'foil.webp', 'chrome.webp', 'sparkle.webp'):
    shutil.copy(n, out + n)
print('written to', out)
