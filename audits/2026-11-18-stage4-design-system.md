# Audit · Stage 4 · Design system (core + brand + layer)

- **Date:** 2026-11-18
- **What changed:** `design/` (core 0.31.1 vendored, `brand/oie`, `product/oie`, generated tokens), `02 Design System/README.md`, the brand rules in `AGENTS.md`, CI.
- **Skills used and how:**
  - `graphic-design-fundamentals`: loaded this session; its §4 colour, §3 type and §13 checks are applied below.
  - `brand-identity-kit`: loaded (phases 3, 7 and 9).
  - `ux-user-flows`, `impeccable`, `less-is-more`: not run, because there is no screen or flow yet. The component rules in README §10 get their audit with the first screens (Stage 5).

## Automated checks
`python3 design/build.py --check`: 0 errors, 0 warnings. It checks every core token in the layer, the brand limits, and 13 contrast pairs in light and dark. A test pair (accent text on page at 4.5) was added and failed as expected (3.36:1), then removed, so the check works.

## graphic-design-fundamentals
| # | Item | Pass? | Note |
|---|---|---|---|
| 3 | Hierarchy | ✅ | Six type roles, each with one job; display only once per screen. |
| 6 | Spacing | ✅ | The core scale only (4–64). |
| 7 | Typography | ✅ | One family (Nunito), Portuguese accents, OFL licence; body 16/24; content-max 600dp keeps lines readable. |
| 8 | Colour | ✅ | Every text pair ≥ 4.5:1 in both themes; the accent is fill-only (3.4:1 light, a graphic); one accent per screen; `selected` keeps picks off the accent; stop is distinct from the accent and always has a word. |
| 9 | Consistency | ✅ | Screens use roles, never colour names; generated files only. |
| 12 | Accessibility | ✅ | 48dp targets, 52dp buttons, 72dp record button, no meaning under 12sp, colour never alone. |
| 13 | Production | 🟡 | Screen ready. CMYK and Pantone values are TBD before print. |

## Findings
| Severity | Finding | Fix | Status |
|---|---|---|---|
| Fixed during the build (would have been P1) | Tokens that point at `{ink}` or `{mint}` took the core's unbranded values: the frame came out mint, text on selected came out the core's ink | Gave Oiê's own tokens explicit brand values | Fixed |
| P3 | Accent coral is close to the stop brick in hue | Stop always carries a word and is darker; review on the first screens | Open |

## What was cut
- Fairy mode (owner's decision: it would bring pink back).
- The boilerplate's placeholder `02 Design System/tokens.json`, replaced by `design/`.
