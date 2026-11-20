# Flow: the kid writes to the parent who owns the WhatsApp

_Designed with `ux-user-flows` on 2026-11-19 (v0.30). The owner: "the kid can't send messages to whoever provides the account. If the WhatsApp is mine, my son can't write to me, only to his mother."_

- **Why it can't work like other contacts:** the tablet *is* the parent's WhatsApp account. WhatsApp doesn't send a message from a number to the same number.
- **Decided with the owner:**
  - Oiê creates, when the parent taps a button, a WhatsApp group with **only the parent**, named after the kid ("Tito"). The kid's messages go there, and **everything the parent writes there reaches the kid**.
  - If WhatsApp refuses a one-person group, Oiê uses the parent's **"Mensagem para você"** instead. There, only the parent's replies to the kid's messages, and messages that start with the kid's name, reach the kid; the parent's own notes stay private.
- **WhatsApp's limit, said plainly:** WhatsApp never notifies the parent of messages sent from their own account, and the tablet is their account. The group won't ring on the parent's phone; the parent sees the unread count when they open WhatsApp, and can pin the group at the top.
- **Job story:** *When my kid wants to talk to me and the tablet uses my WhatsApp, I want a chat between us, so he can reach me like he reaches his mother.*

## Steps
| # | Where | Who | The system |
|---|---|---|---|
| 1 | Parent area › Contatos, at the top | The parent sees "Você (Ana)" and **Criar conversa com Tito**, with one line saying what it does and the notification limit | Nothing is created until the parent taps. |
| 2 | Same | Taps Criar conversa com Tito | "Criando…". Oiê creates the group "Tito" on the parent's WhatsApp (only the parent in it) and adds it to Tito's contacts as **Ana**, with the parent's picture. |
| 3 | Kid home | Tito sees Ana with the rest of the family | — |
| 4 | Kid's chat with Ana | Writes, sends | It appears in the group "Tito" on the parent's phone, signed **Tito**. |
| 5 | Parent's phone, the group "Tito" | Writes | It reaches Tito (quiet hours apply, as for anyone). |

## Branches
| At | If | Then |
|---|---|---|
| 2 | WhatsApp refuses the group | The line becomes the parent's "Mensagem para você", and Contatos says so in one line: "O WhatsApp não aceitou o grupo. Tito vai escrever na sua Mensagem para você; para responder, deslize a mensagem dele ou comece com o nome dele." |
| 2 | Not connected | The button waits for the connection ("Conectando…"); nothing is created. |
| any | The parent removes Ana from Contatos | Tito can't write to Ana any more. The group stays in the parent's WhatsApp (the parent can leave it there); "Criar conversa com Tito" comes back. |
| any | The parent types a command there ("/Tito parar") | It's a command, as in any chat (pause, resume). |
| any | The parent renames Ana ("Papai") | As for any contact (v0.19). |

## Notes
- **Product law:** the group has only the parent; nobody else is added, ever.
- **Rule L2 (decision 0007):** the parent's own chat receives the kid's messages only in the fallback, when the parent made it the line. Otherwise it still gets only Oiê's command answers.
- **Data:** nothing new leaves the tablet. The group lives in the parent's own WhatsApp.

## Flow review (ux-user-flows §13)
| # | Question | Result |
|---|---|---|
| 1 | Job | ✅ |
| 2 | Entries | ✅ Contatos, behind the PIN. |
| 3 | Happy path | ✅ One tap for the parent; for the kid it's another contact. |
| 4 | Branches | ✅ Refused group, offline, removed, commands, renamed. |
| 6 | Errors | ✅ The refusal falls back and says so. |
| 7 | Control | ✅ Nothing is created without the tap; removing is the usual Remover with undo. |
| 14 | Ethics | ✅ The notification limit is said before creating, not discovered later. |
| 15 | Evidence | 🟡 Whether WhatsApp accepts a one-person group from a linked device: the owner's test. |

## v0.35: the chat marks itself unread
_The owner's report: the line works, but the parent's phone doesn't show it. WhatsApp never notifies an account about its own messages, and the kid writes from the parent's account._

- **What changes:** after each message the kid sends on the line to the parent, the tablet uses WhatsApp's own **"Marcar como não lida"** on that chat. It syncs to the parent's phone like anything done on WhatsApp Web: the chat gets the unread dot and moves to the top of the list.
- **What doesn't:** there's still no sound or banner. An unread mark isn't a notification, and WhatsApp doesn't let an account notify itself. The help says so.
- **Only on the line to the parent.** Chats with anyone else are never touched; there, the family get WhatsApp's normal notification.
- **When the parent opens the chat,** WhatsApp clears the mark as usual. The next message from the kid sets it again.
- **If marking fails** (for example, right after linking, before WhatsApp has shared its settings with the tablet), the message has still gone. The report notes it, and the next message tries again.

| # | Question | Result |
|---|---|---|
| 3 | Happy path | ✅ Nothing changes for the kid; nothing to do for the parent. |
| 4 | Branches | ✅ Marking fails: the message is unaffected, the next one tries again. |
| 14 | Ethics | ✅ It only touches the chat Oiê created for the kid; the parent's other chats are never changed. |
