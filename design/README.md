# Oiê design: core + brand + product

Oiê builds on the **Gabriel Design System core**, the same way Cochilo does (the design system's
`docs/products.md`). Three layers in, generated files out:

```
design/
  core/            the design system's core, vendored unchanged at 0.31.1 (core/CORE.md)
  brand/oie/       the Oiê brand: colour roles and the one type family (brand.json, brand.md)
  product/oie/     Oiê's own layer: themes, the core tokens it pulls, its own tokens, contrast pairs
  build.py         puts them together and runs the core's generator and checks
  generated/       OTokens.kt (Compose, package app.oie.ui.theme) and tokens.css (website). Never edit.
```

```sh
python3 design/build.py          # write design/generated/
python3 design/build.py --check  # layer, brand and contrast checks; fails if generated/ is stale
```

## Where a change goes
1. **Could any of the owner's apps use it?** Then it goes to the design system as a PR there, and comes back here by re-syncing `core/`.
2. **Is it Oiê's look (a colour role, the type family)?** It goes in `brand/oie/brand.json`.
3. **Is it only Oiê's (a size, a component, its own colour)?** It goes in `product/oie/layer.json`. Add every colour pair it uses to `contrast` there.

What the core brings, and Oiê keeps:
- spacing 4, 8, 12, 16, 24, 32, 40, 48, 64;
- radii 4 (controls), 8 (cards), 16 (pills, sheets), round;
- motion 150 / 250 ms;
- the 4dp frame round every screen (coral here);
- 48dp targets and 52dp buttons;
- nothing that carries meaning under 12sp;
- contrast in every theme;
- self-update.

**Not carried:** fairy mode. That was the owner's decision on 2026-11-18, because it would bring pink back and the brand bans pink.
