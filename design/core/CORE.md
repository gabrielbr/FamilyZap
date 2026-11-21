# The vendored core

`design/core/` is a copy of the Gabriel Design System's core. Oiê builds against it, so it
doesn't need the design system's repo to build.

| | |
|---|---|
| Copied from | `gabrielbr/Gabriel-Deisgn-System`, release **0.31.1** (`main` at `ab22916`) |
| Files | `core.json` (the `core` section of `tokens/tokens.json`); `scripts/tokens_lib.py`, `gen_tokens.py`, `check_tokens.py` (unchanged copies) |
| Copied on | 2026-11-18 (Oiê design 1.0.0) |
| Icons | `icons/gg_ic_*.xml`: the line icons Oiê uses, copied unchanged from the design system's `assets/icons/android/` at **0.32.0** (which added camera, image, sticker, send, link, clock); `gg_ic_heart` added in Oiê 0.42.0, unchanged from the design system at 0.32.1 (favorites) |

## Rules
- **Never edit these files here.** A core change is made in the design system and copied in. If
  `build.py` needs something the core doesn't offer, add it to the core upstream, or, if it's
  only Oiê's, to `../product/oie/`.
- Brand and product data are not in `core.json`: they live in `../brand/oie/` and `../product/oie/`.

## Re-syncing to a new core release
```sh
DS=../Gabriel-Deisgn-System   # a checkout of the design system at the release tag
cp $DS/scripts/tokens_lib.py $DS/scripts/gen_tokens.py $DS/scripts/check_tokens.py design/core/scripts/
python3 -c 'import json,sys; t=json.load(open(sys.argv[1])); json.dump({"$comment": "Vendored from the Gabriel Design System (design/core/CORE.md).", "name": t["name"], "version": t["version"], "core": t["core"]}, open("design/core/core.json","w"), ensure_ascii=False, indent=2)' $DS/tokens/tokens.json
python3 design/build.py && python3 design/build.py --check
```
Then update the table above and look at what moved in `design/generated/` (`git diff`).
