# Flow: family statuses (v0.36: watch, react, reply)

_Designed with `ux-user-flows` on 2026-11-20. Decided with the owner:_
- _watching marks the status as viewed (the family see the parent's name, since Oiê uses the parent's WhatsApp);_
- _only the family's statuses, never the parent's own;_
- _off until the parent turns it on;_
- _posting comes in v0.37, after a patch to the link library._

**Job:** _When a grandparent posts a photo of the cake in their status, Tito wants to see it, react and answer, like the rest of the family, without anyone else's status reaching him._

## Product law
- **Only statuses from people the parent allowed.** Never from groups (they have none), never from paused people, never from the parent's own account.
- **Nothing from before Oiê was linked** (rule L6).
- **A status lasts 24 hours on the tablet,** then it's deleted, as in WhatsApp. A status its owner deletes goes too.
- **Reactions and replies go to the poster's own chat.** Never to WhatsApp's "status broadcast", which would reach the parent's whole status audience. The kid can only reach people they're allowed to reach.

## Entries
- The kid's home, a **Novidades** row above the people. It shows only when someone allowed has a status from the last 24 hours, and only when the parent turned statuses on.

## Steps
| # | Screen | Kid does | Oiê does |
|---|---|---|---|
| 1 | Home | Sees "Novidades": a round photo per person, with a coral ring while it has something unseen and a hairline once seen. The name is under it, and "nova" in words, so the meaning isn't in the colour alone. | People with unseen statuses come first. |
| 2 | Status viewer | Taps a person | Shows their statuses, oldest first, full screen. There's a segment line on top, one segment per status, and the name and "há 2 h" in the bar. Photos and text stay 5 s, then move on; a video plays to its end. Tap the right half for the next, the left half for the previous. **Pausar** holds it. **Back** closes it. Each status shown is marked viewed. |
| 3 | Same | Taps a reaction (the six) | Sends it as a reaction to that status, to the poster's chat. Tapping the same one again takes it away. |
| 4 | Same | Taps **Responder**, types, **Mandar** | The viewer pauses. The reply goes to the poster's chat, quoting the status, and also shows in the kid's chat with them ("Resposta a uma novidade"). A snackbar says "Mandado para Vovó". |
| 5 | Home | — | After the last status, the viewer closes and the ring turns to a hairline. |

## Branches
| At | If | Then |
|---|---|---|
| 1 | Statuses off (the default) | No row, nothing kept. Statuses that arrive are dropped. |
| 1 | Nobody has a status | No row (less-is-more: no empty "Novidades"). |
| 2 | The photo or video is still arriving | "Chegando…" in its place; it waits there, without moving on. |
| 2 | A video too big to keep (over 64 MB) | "Essa novidade só abre no WhatsApp de {pai/mãe}." Then it moves on. |
| 2 | A text status | The text, large, on the `selected` ground (the poster's own colour isn't used: tokens only). |
| 2 | The owner deletes it while it's open | It closes, and it's gone from the row. |
| 2 | Remove animations | The segment line jumps instead of filling. |
| 4 | The link is down | Mandar is off, and the line says "Peça para {nome} conectar de novo", as in a chat. |
| 4 | Quiet hours | Mandar and the reactions are off, and the line says the quiet hours' end, as in a chat. |
| any | 24 hours pass | The status is deleted from the tablet. |

## Parent
- **Ajustes e ajuda › Novidades:** "Ver novidades da família", off.
- Its line says what happens: "Tito vê as novidades de quem está em Contatos e pode reagir e responder. Quando Tito vê uma, a pessoa vê que você viu, porque é a sua conta."
- Help explains the same.

## Flow review (ux-user-flows §13)
| # | Question | Result |
|---|---|---|
| 1 | Job | ✅ One actor, one job. |
| 2 | Entries | ✅ One, the home row. |
| 3 | Happy path | ✅ Tap a face, watch, done. A reaction is one tap; a reply is two taps and the text. |
| 4 | Branches | ✅ Off, none, arriving, too big, text, deleted, motion, offline, quiet hours, expiry. |
| 5 | States | ✅ As above. |
| 7 | Control | ✅ Back, Pausar, next and previous, a reaction tapped again. |
| 12 | Speed | ✅ Photos wait while arriving; nothing jumps ahead of the kid. |
| 13 | Accessibility | ✅ The tap halves are also "Anterior" and "Próxima" for TalkBack; the ring has the word; reactions are labelled. |
| 14 | Ethics | ✅ No counts, no streaks, nothing from anyone else; the parent knows views show as them. |
| 15 | Evidence | 🟡 To check on the device: WhatsApp shows the kid's reaction on the poster's status. |
