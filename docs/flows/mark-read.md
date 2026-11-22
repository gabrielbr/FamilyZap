# Flow: What reaches the kid is marked read on the parent's WhatsApp (v0.55)

_Designed with `ux-user-flows` on 2026-11-22, at the owner's request: "Messages sent to the kid can be marked as read on the parent's device? So it avoids bombarding the parent with notifications that are not for them." The owner chose "as soon as it arrives", on by default, with a switch._

- **Actor and job story:** the parent, whose phone is the bridge. *When the family writes to Tito all day, I want my phone to stop showing those messages as unread for me, so my notifications are for what's actually for me.*
- **Success measure:** a message that reached the kid shows read on the parent's phone within seconds. What stays with the parent stays unread.

## Steps
| # | What happens | Where |
|---|---|---|
| 1 | Vovó writes "Tito, domingo tem bolo" to the parent's WhatsApp. | Vovó's phone |
| 2 | The parent's phone notifies, as WhatsApp always does (Oiê can't stop the first buzz). | Parent's phone |
| 3 | The tablet receives it; the message rule says it's for Tito (shown, or kept quietly in quiet hours). | Tablet |
| 4 | Oiê sends WhatsApp's read receipt for it. | Tablet |
| 5 | The parent's phone marks the chat read and clears its notification. Vovó sees the blue ticks. | Parent's phone, Vovó's phone |

## Branches
| At | If | Then |
|---|---|---|
| 3 | The message is for the parent (no name, talk about the kid, the parent took over) | Not marked: it stays unread on the parent's phone. |
| 3 | It's on the line to the parent, the parent's own message, or a deleted one | Not marked. |
| 4 | The parent turned it off (Ajustes › Mensagens para Tito) | Not marked, as before v0.55. |
| 4 | The tablet is offline | The message arrives when it's back, and is marked then. |
| 5 | The parent's phone keeps the notification | Some phones clear notifications when a chat is read elsewhere a little later; WhatsApp decides. |

## Honesty
- **The blue ticks:** the sender sees them as soon as the tablet has the message, even before Tito opens it. The switch's help line says so, plainly.
- **The parent still sees everything:** the messages are in WhatsApp as always; they're just not left unread.

## Flow review (ux-user-flows §13)
| # | Question | Answer |
|---|---|---|
| 1 | Job | ✅ Notifications that are for the parent. |
| 4 | Branches | ✅ For the parent, the parent's line and own messages, off, offline. |
| 7 | Control | ✅ One switch in Ajustes, on by default (the owner). |
| 14 | Ethics | ✅ The ticks are said plainly. Nothing is hidden from the parent. A receipt is what any WhatsApp sends, not a message. |
| 15 | Evidence | 🟡 Send Tito a message from another phone and watch the parent's phone. |
