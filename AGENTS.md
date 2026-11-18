# Working in this repo (for agents)

FamilyZap lets a kid aged 9–12 message and call family from an Android tablet through a parent's WhatsApp, only with contacts the parent allowed. Read [PRODUCT.md](PRODUCT.md) and [STATUS.md](STATUS.md) first.

This project follows the Gabriel Product Boilerplate playbook (PLAYBOOK.md in that repo).

## Brand rules
1. **The one idea:** idea, tagline, what the brand never implies.
2. **Voice and copy:** the rules from `voice.md`, banned words, buttons verb-first, no invented data.
3. **Tokens:** every colour, size, radius and duration comes from the tokens. No literals in screens. Never hand-edit generated token files.
4. **Type:** families, scale, one h1 per page, line length.
5. **Logo:** use the files only; never retype or redraw. Clear space, minimum sizes.
6. **Graphic rule:** the one device and where it may appear.
7. **Texture and motion:** pausable; honours reduced motion; nothing that carries meaning is animated only.
8. **Components and layout:** screens compose the component kit; one primary action per screen; 390 px works.
9. **Before you finish, check:** contrast in every theme, colour never alone, targets ≥44–48, copy against the voice, the audit file written.

## Design work
Every new or changed screen, flow, page or asset is designed with, and audited against, the skills in `.claude/skills/`, in this order:
1. ux-user-flows
2. impeccable
3. graphic-design-fundamentals
4. less-is-more

The audit goes in `audits/YYYY-MM-DD-<version>-<topic>.md`. Fix P0 and P1 before merging. Never claim a skill was used without loading it.

## How changes go in
- Work on a branch and open a PR. **Always merge it yourself (squash) once the work is finished and verified**: checks green, reviews resolved, no open P0/P1. Don't leave PRs waiting (the owner's standing instruction, 2026-11-18). Then release.
- **Docs on every change.** A change without its docs isn't done. Update: README, CHANGELOG (plain words), `docs/<feature>.md`, `docs/roadmap.md`, `docs/decisions/`.
- Never skip or loosen a test to get green.

## Product law
- **Data:** messages and contacts are never stored on our servers; the WhatsApp link runs on the kid's tablet. No ads, no tracking of kids.
- **Honesty about the bridge:** FamilyZap uses WhatsApp unofficially (decision 0001). Parents are told before linking that their number could be restricted or banned. Never hide or soften this, and never bulk send.
- **Contacts:** the kid can never reach, or be reached by, a contact the parent didn't allow.
- **Claims:** never claim to make a child "100% safe" or to replace a parent's attention. Never imply it's made or endorsed by WhatsApp or Meta; no WhatsApp logo.
- **Stop and ask before:** deploying, sending messages, moving money, deleting data, renaming ids.

## Before you push
```sh
# replace with this project's real commands
<token check>  &&  <unit tests>  &&  <lint>  &&  <build>  &&  <screenshot tests>
```
