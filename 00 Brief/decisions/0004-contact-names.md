# 0004 · Names for the kid live on the tablet

- **Date:** 2026-11-19
- **Status:** approved
- **Approved by:** Owner (asked for it, 2026-11-19: "for my kid, I want them to be grandpa and grandma")

## Context
Parents save family under their real names ("Maria Silva"). A kid of 9–12 knows them as Vovó and Vovô. Oiê showed the WhatsApp name. The owner also wants to see each contact's number in the parent area, to be sure the right person was added.

## Decision
- The parent can give each allowed contact the name the kid sees. It is kept on the tablet only, next to the WhatsApp name (`Contact.waName`), and never written to WhatsApp.
- The parent area shows the number (from the WhatsApp link) and, when renamed, "Maria Silva no WhatsApp". The kid's side shows only the chosen name.
- A blank name goes back to the WhatsApp name.

## Options considered
| Option | Why not |
|---|---|
| Rename the contact in the parent's WhatsApp (app-state sync) | It changes the parent's phone, writes to WhatsApp for no need (rule L1: read and send only what's needed), and the parent didn't ask for it. |
| Show the number to the kid too | The kid doesn't need it; it's the parent's check. |

## Consequences
- `Contact` gains `phone` and `waName`, saved on the tablet. The link's contact list now carries the number (`phone`), also for contacts WhatsApp knows by a private id, when it has the mapping.
- Removing a contact moved from each row into its editor, so a phone row has room for the number (v0.19 audit).
