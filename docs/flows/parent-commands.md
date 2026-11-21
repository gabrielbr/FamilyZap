# Flow: commands from the parent's WhatsApp

_Designed with `ux-user-flows` on 2026-11-19 (v0.21), from the owner's request: "/Tito" sent from the parent's WhatsApp gets a list of commands, such as "/Tito stop" to stop a conversation reaching the kid. Decided with the owner: Oiê answers only in the parent's own chat ("Mensagem para você"); stop **pauses** the chat, it doesn't remove it._

- **Actor and job story:** the parent, away from the tablet. *When I'm in a chat on my phone (Vovó, or the family group) and want to change whether my kid is in it, I want to do it right there, so I don't have to wait to get to the tablet.*
- **Success measure:** pause, resume, add or remove a chat in one message from the phone, with an answer in the parent's own chat within seconds while the tablet is online.
- **Who can use it:** only the parent's own WhatsApp account (messages "from me"). Anyone else typing "/Tito parar" does nothing: the product law says no stranger can reach the kid or change who can.
- **Entry points:** any chat on the parent's phone (the command acts on that chat), or the parent's own chat (help only).

## The commands
Any of the kid's names works after the slash ("/Francisco", "/Tito", "/Chico"). Capitals and accents don't matter. Portuguese first; the English words work too.

| Command | Does | Answer in the parent's own chat |
|---|---|---|
| `/Tito` (or `/Tito ajuda`, `help`) | Nothing changes | What this chat is for the kid now (receives, paused, not in the list) and the commands. |
| `/Tito parar` (`pausar`, `stop`, `pause`) | Pauses this chat: nothing from it reaches the kid, and it leaves the kid's home until resumed. Its name and settings stay. | "Pausado: Vovó não chega mais para Francisco. Para voltar: /Tito voltar" |
| `/Tito voltar` (`continuar`, `retomar`, `start`, `resume`) | Resumes a paused chat. | "Vovó voltou para Francisco." |
| `/Tito adicionar` (`add`) | Adds this chat to the kid's contacts, with its WhatsApp name (a group with the group warning). If it was paused, resumes it. | "Vovó agora pode falar com Francisco." / for a group: "…todos do grupo veem o que Francisco manda." |
| `/Tito remover` (`remove`) | Removes this chat from the kid's contacts, like Remover on the tablet. | "Vovó saiu dos contatos de Francisco." |
| anything else after the name | Nothing changes | "Não entendi '…'." and the list. |

## Steps (pause from the phone)
| # | Where | The parent | The system |
|---|---|---|---|
| 1 | Chat with Vovó, on the phone | Sends "/Tito parar" | WhatsApp delivers it to Vovó too (she sees the command; nothing else). |
| 2 | The tablet | Nothing | Oiê sees a command from the parent's own account, in an allowed chat, and pauses it. Nothing is stored about the message. |
| 3 | The parent's own chat | Reads "Pausado: …" | The kid's home no longer shows Vovó; her messages stay on the parent's phone only. |
| 4 | Later, same chat | Sends "/Tito voltar" | Resumed; "Vovó voltou para Francisco." |

## Branches
| At | If | Then |
|---|---|---|
| Any command | Sent by anyone but the parent's account | Ignored. It's an ordinary message, under the usual rule (a "/Tito" from Vovó is not for the kid either: it starts with a slash). |
| Any command | Sent in the parent's own chat | Only the help, with "Mande o comando na conversa que você quer mudar." |
| parar / voltar / remover | The chat isn't in the kid's list | "Esta conversa não está nos contatos de Francisco. Para incluir: /Tito adicionar" |
| parar | Already paused | Same answer as pausing; nothing changes. |
| adicionar | Already in the list | "Vovó já pode falar com Francisco." |
| Any | The tablet is offline or asleep | WhatsApp keeps the command; Oiê acts when it connects again, then answers. A command older than 24 hours is ignored (it may no longer be what the parent wants). |
| Any | The kid's side isn't on the real WhatsApp yet | Ignored. |

## States on the tablet
- **Paused chat, kid's side:** not on the home screen; nothing from it arrives; it can't be written to.
- **Paused chat, parent area:** stays in Contatos with "Pausado pelo WhatsApp" under the number, instead of the quiet-hours line. Its editor has **Voltar a receber**.

## Notes
- **What Vovó sees:** only the parent's "/Tito parar". Oiê's answers go to the parent's own chat, so nobody else gets a menu (owner's choice).
- **Rule L2:** the parent's own chat receives only these answers; the kid can never write there.
- **Privacy:** the command text is never stored; the problem report says "command: pause" without names or numbers.

## Flow review (ux-user-flows §13)
| # | Question | Result |
|---|---|---|
| 1 | Job | ✅ One actor, one job: change a chat for the kid without the tablet. |
| 2 | Entries | ✅ Any chat; the own chat gives help. |
| 3 | Happy path | ✅ One message. |
| 4 | Branches | ✅ Not the parent, own chat, not in list, already, offline, old, not real yet. |
| 6 | Errors | ✅ Unknown word gets the list back, never silence. |
| 7 | Control | ✅ Every command has its undo (parar ↔ voltar, adicionar ↔ remover). |
| 9 | Memory | ✅ `/Tito` alone lists everything, with the chat's current state. |
| 11 | Conventions | ✅ Slash commands, as in chat bots. |
| 13 | Accessibility | ✅ Plain text in WhatsApp; no hidden gesture. |
| 14 | Ethics | ✅ Only the parent's account; the kid's own notice already says the parent controls the contacts. 🟡 A paused chat just leaves the kid's home without a word; the owner's test will say if the kid needs a line ("Ana pausou esta conversa"). |
| 15 | Evidence | 🟡 Do families find the "/Tito" message in their chat odd? The owner's test. |

## v0.43: from your own chat ("Mensagem para você")
_The owner, 2026-11-21: "extend the commands I can do from my own chat … List all contacts, remove contact, add contact (followed by the actual contact card WhatsApp shares) and others related to the settings." The owner chose: pause/resume by number, the kid-only switch, quiet hours on/off now; and "command, then card"._

Typed in the parent's own chat, a change needs to say **who**: a number from the list, or a name (or part of one) that matches one contact only. Typed in a contact's chat, the commands act on that chat, as before.

| Command (PT · EN) | What it does | Answer |
|---|---|---|
| `/Tito contatos` · `contacts` | Lists the kid's contacts, numbered (never the line to the parent), with "grupo", "pausado" and "só de Tito". | "Oiê · contatos de Tito: 1. Vovó · 2. Tia Bia · pausado · 3. Primos · grupo · só de Tito" |
| `/Tito parar 2` · `voltar 2` · `remover 2` (or a name) | As in the chat, for that contact. | As in the chat. |
| `/Tito só 2` · `own 2` (also "só dele/dela") | Turns on "Conversa só de Tito" for that contact (people and groups; never the parent line). | "Oiê · conversa só de Tito: tudo de Primos chega para Tito." |
| `/Tito normal 2` | Turns it off. | "…voltou ao normal: …" |
| `/Tito adicionar` · `add` | Opens a 10-minute window: each contact card the parent shares in this chat is added (or resumed if paused). Several cards at once work. | "Oiê · agora compartilhe aqui o cartão do contato (Anexar › Contato)…", then per card "Lucas agora pode falar com Tito." / "já pode" / "…não tem WhatsApp…" |
| `/Tito silêncio` · `quiet` | Quiet hours **now**, with no end: the kid reads and listens but sends nothing. Exceptions (people allowed in quiet hours) still apply. | "Oiê · silêncio agora: Tito lê e ouve, mas não manda nada. Para liberar: /Tito liberar" |
| `/Tito liberar` · `release` | Lifts it. Scheduled periods still apply. | "Oiê · liberado: …" |
| `/Tito parar` with no number, in the own chat | Changes nothing. | "Oiê · diga quem, com o número de /Tito contatos ou o nome: por exemplo /Tito parar 2" |
| `/Tito parar 9` (no such contact) | Changes nothing. | "Oiê · não achei “9” nos contatos…" |

**Contact cards:**
- **Read only in the parent's own chat, from the parent's own account, inside the 10 minutes after "/Tito adicionar".** A card at any other time, or anywhere else, is never added. In other chats a card still shows to the kid as "só abre no WhatsApp", as before.
- **The number comes from the card's WhatsApp id (`waid`).** A card without one isn't on WhatsApp and is refused.

**On the tablet:**
- **Silêncio:** the kid's home and chats say "Horário de silêncio até Ana liberar."
- **The parent's Horário de silêncio** shows "Silêncio agora, ligado pelo seu WhatsApp." with **Liberar agora**.
- **Saved:** the setting survives a restart.

**Product law:** only the parent's own account can command, and only allowed contacts are ever touched. Adding still means the parent chose that person: the card they shared.
