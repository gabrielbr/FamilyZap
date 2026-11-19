# 0007 · A chat between the kid and the parent who owns the WhatsApp

- **Date:** 2026-11-19
- **Status:** approved
- **Approved by:** Owner (proposed the group; chose the button in Contatos and the fallback rule, 2026-11-19)

## Context
The tablet is the parent's WhatsApp account, so the kid couldn't write to that parent: WhatsApp doesn't send from a number to itself.

## Decision
- On the parent's tap, Oiê creates a WhatsApp group with only the parent, named after the kid. It's added to the kid's contacts as the parent, and everything the parent writes there reaches the kid.
- If WhatsApp refuses a one-person group, the line is the parent's "Mensagem para você", and only the parent's replies to the kid, and messages starting with the kid's name, reach the kid.
- Rule L2 changes: the parent's own chat may receive the kid's messages, only when it's the fallback line.

## Options considered
| Option | Why not |
|---|---|
| "Mensagem para você" only | The parent's notes and stickers live there; the owner preferred a separate chat. |
| A second WhatsApp number | Not every family has one. |

## Consequences
- **No notification:** WhatsApp never notifies the parent of messages from their own account. This is said in Contatos before the group is created.
- The link gets `CreateParentLine`, `OwnChat` and `SetParentLine`. `Contact.parentLine` marks the line.
