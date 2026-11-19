# 0005 · Commands from the parent's WhatsApp

- **Date:** 2026-11-19
- **Status:** approved
- **Approved by:** Owner (asked for "/Tito" commands; chose answers in their own chat, and "stop" as pause, 2026-11-19)

## Context
The parent is often away from the tablet but always has their phone. They want to change, from any chat, whether that chat reaches the kid.

## Decision
- A message **from the parent's own account** that starts with a slash and one of the kid's names ("/Tito", "/Chico parar") is a command. It acts on the chat it was typed in: help, pause, resume, add, remove.
- Oiê answers **only in the parent's own chat** ("Mensagem para você"). The people in the chat see the parent's command and nothing else.
- "Parar" **pauses**: the chat keeps its place, name and settings, and reaches the kid in no way until "voltar".
- Commands from anyone else are ordinary messages. A command older than 24 hours is ignored.

## Options considered
| Option | Why not |
|---|---|
| Answer in the same chat | Vovó or the whole group would get a menu and confirmations (the owner chose against it). |
| Answer, then delete for everyone | Leaves "message deleted" marks and adds sends to other people's chats. |
| "Parar" removes | Loses the name the parent gave and the settings; pause is easier to undo. |
| Commands from family members too | Product law: no one but the parent decides who reaches the kid. |

## Consequences
- Rule L2 gains one exception: the parent's own chat receives Oiê's answers, and nothing else, never from the kid.
- `Contact.paused`; the kid's side hides paused chats; Contatos shows "Pausado pelo seu WhatsApp" with "Voltar a receber".
- The link adds `SendSelfNote`; the app adds `ParentCommand` and `KidNames`.
