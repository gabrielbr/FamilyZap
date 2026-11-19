# Working in this repo (for agents)

Oiê lets a kid aged 9–12 message and call family from an Android tablet through a parent's WhatsApp, only with contacts the parent allowed. Read [PRODUCT.md](PRODUCT.md) and [STATUS.md](STATUS.md) first.

This project follows the Gabriel Product Boilerplate playbook (PLAYBOOK.md in that repo).

## Brand rules
The full system is in [`02 Design System/README.md`](02%20Design%20System/README.md); the values are in [`design/`](design/README.md).
1. **The one idea:** Family, within reach. Tagline: "Send love home". Oiê never implies that it's a WhatsApp product, that a child is 100% safe, that the WhatsApp link is risk-free, or that it's for spying.
2. **Voice and copy:** [`01 Brand/voice.md`](01%20Brand/voice.md).
   - Warm, playful, plain: say it like you'd say it at the table. Informal "você"; Portuguese first.
   - No "!" except in "Oiê!"; no emoji in UI text; no em dashes; buttons start with a verb.
   - Never: hype words, Zap/Wpp, tech words (session, server, linked device) in the UI. No invented data.
3. **Tokens:** every colour, size, radius and duration comes from `design/generated/` (`OTokens.kt`, `tokens.css`). No literals in screens.
   - Change values in `design/brand/oie/` or `design/product/oie/`, then run `python3 design/build.py`. Never hand-edit `generated/` or `design/core/`.
   - Spacing is 4, 8, 12, 16, 24, 32, 40, 48, 64.
4. **Type:** Nunito only. Use the roles: display, title, label, body, body-small, caption. Nothing that carries meaning under 12sp.
5. **Logo:** use the files in `01 Brand/logo/` only; never retype or redraw. Clear space is the i's dot; the wordmark minimum is 64 px. Never drop the circumflex.
6. **Graphic rule:** the coral accent is one filled thing per screen. Picked things use `selected`. A 4dp coral frame goes round every screen. Colour is never the only carrier of meaning.
7. **Texture and motion:** the touching rounds from the logo, at most one cluster per screen, never behind text. Motion 150/250 ms, no bounce, press scales to 97%. Honour "Remove animations".
8. **Components and layout:** screens compose the components in the design system README §10.
   - One main action per screen; 48dp targets, 52dp buttons.
   - Icons: the design system's line set (22dp, stroke 1.75) with labels. Never another set, never emoji.
   - Works at 360dp phone width and on the tablet (column max 600dp).
9. **Before you finish, check:**
   - `python3 design/build.py --check` passes;
   - contrast holds in light and dark;
   - colour is never alone;
   - copy follows the voice;
   - the audit file is written.

## Design work
Every new or changed screen, flow, page or asset is designed with, and audited against, the skills in `.claude/skills/`, in this order:
1. ux-user-flows
2. impeccable
3. graphic-design-fundamentals
4. less-is-more

The audit goes in `audits/YYYY-MM-DD-<version>-<topic>.md`. Fix P0 and P1 before merging. Never claim a skill was used without loading it.

**Never rush (the owner's standing instruction, 2026-11-19).** No change is done quickly or partly. Every change, however small, goes through the whole process, in this order:
1. **Design.** Write or update the flow (`docs/flows/`) with ux-user-flows before building.
2. **Develop.** Build it, with tests for the logic.
3. **Audit.** Run all four skills, loaded and read:
   - look at every changed screen in the tablet and phone screenshots (`ScreensTest`, `PhoneScreensTest`), light and dark, normal and large text;
   - score the result;
   - write the audit file.
4. **Fix.** Fix every P0 and P1, re-record the screenshots and look at them again.
5. **Docs.** Update README, CHANGELOG, STATUS, the flow, the roadmap and the decisions.
6. **Ship.** Run the full check, open the PR, merge when green, release.

A fix found during the audit goes through the same steps.

## How changes go in
- Work on a branch and open a PR. **Always merge it yourself (squash) once the work is finished and verified**: checks green, reviews resolved, no open P0/P1. Don't leave PRs waiting (the owner's standing instruction, 2026-11-18). Then release.
- **Docs on every change.** A change without its docs isn't done. Update: README, CHANGELOG (plain words), `docs/<feature>.md`, `docs/roadmap.md`, `docs/decisions/`.
- Never skip or loosen a test to get green.

## Product law
- **Data:** messages and contacts are never stored on our servers; the WhatsApp link runs on the kid's tablet. No ads, no tracking of kids.
- **Honesty about the bridge:** Oiê uses WhatsApp unofficially (decision 0001). Parents are told before linking that their number could be restricted or banned. Never hide or soften this, and never bulk send.
- **Contacts:** the kid can never reach, or be reached by, a contact the parent didn't allow.
- **Claims:** never claim to make a child "100% safe" or to replace a parent's attention. Never imply it's made or endorsed by WhatsApp or Meta; no WhatsApp logo.
- **Stop and ask before:** deploying, sending messages, moving money, deleting data, renaming ids.

## Before you push
```sh
python3 design/build.py --check \
  && ./gradlew testDebugUnitTest verifyRoborazziDebug lintDebug assembleDebug
```
- Screenshots cover the tablet (landscape and portrait, light and dark) and, since v0.13, a 360dp phone and 1.3× text (`PhoneScreensTest`). A layout change looks at both.
- A screen change re-records its screenshots on purpose: `./gradlew recordRoborazziDebug`, then look at every changed PNG in `app/src/test/screenshots/` before committing.
- The app needs `link/oielink.aar`: build it once with `link/build-aar.sh` (Go, Android SDK and NDK r27c). A change in `link/` also runs `cd link && go test ./...` and rebuilds the AAR, and follows the rules L1–L9 in `docs/whatsapp-link.md`.
- The kid side runs on the real WhatsApp (`RealLink`, since v0.12 there is no sample in the app), through `RealInbox` then `Inbox.decide`. `SampleLink` is test data for the screenshot tests only. The real link goes behind the same `WhatsAppLink` interface and must keep the product law.
- Network use: self-update (`update/`, GitHub's public API) and the WhatsApp link, once the parent linked it (in the background too, through `LinkService`). It never sends anything about the family. Releases follow `docs/release.md`.
