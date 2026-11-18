# Flow: Choose contacts + quiet hours

_Stage 5. Designed with `ux-user-flows` on 2026-11-18. The owner decided per-contact exceptions to quiet hours (2026-11-18). Defaults marked **(proposed)** are Claude's, for the owner to confirm._

- **Actor and job story:** the parent, in the parent area on the kid's tablet. *When my kid's circle changes (a new cousin, a grandparent's new number) or the routine changes (school term, holidays), I want to change who they can talk to and when, in a minute, so I can keep the circle right without supervising every message.*
- **Success measure:**
  - adding or removing a contact takes 3 taps after the PIN, under 30 s;
  - a quiet-hours change takes under 1 minute;
  - no kid ever reaches a contact that isn't on the list (product law, tested).
- **Entry points:**
  1. Setup steps 5–6 (flow "Parent setup + link").
  2. **Área dos pais**: a small "Pais" button in the corner of the kid's home, then the PIN.
  3. **A request from the kid (proposed):** the kid taps "Pedir para adicionar alguém" on the empty home, and the parent sees "{criança} pediu para adicionar alguém" in the parent area. No free text, so no way to reach a stranger.
- **Preconditions:** setup done; the WhatsApp link is connected for loading contacts. Without it, the saved list still shows.
- **Taps on the common path:** add a contact: Pais › PIN › Contatos › + › search › tap the name. That's 3 taps after the PIN, plus typing a few letters.

```mermaid
flowchart TD
  start([Kid home: tap Pais]) --> pin[PIN]
  pin -- wrong 5 times --> wait[Wait 1 minute, Esqueci o PIN offered]
  pin -- ok --> area[Área dos pais<br/>Contatos · Horário de silêncio · Conexão · Sobre]
  area --> contacts[Contatos: the allowed list]
  contacts -- + Adicionar --> search[Search WhatsApp contacts and groups] -- tap --> added[Added, shows on the kid home now<br/>snackbar Desfazer]
  contacts -- swipe or tap Remover --> removed[Removed from the kid home<br/>snackbar Desfazer]
  contacts -- tap a contact --> detail[Contact: name, photo,<br/>'Pode falar no horário de silêncio' switch]
  area --> quiet[Horário de silêncio: list of periods]
  quiet -- + Adicionar período --> period[Name, days, start, end] --> quiet
  quiet -- switch a period off/on --> quiet
  area -- Sair, or 2 minutes idle --> kid([Back to the kid home])
```

## Steps
| # | Screen | What the parent does | What the system does |
|---|---|---|---|
| 1 | **Kid home › Pais** | Taps the small "Pais" button (48dp, a corner, muted, not coral) | Opens the PIN pad. The kid can see the button, but the PIN keeps the area shut. |
| 2 | **PIN** | Types 6 digits | Wrong PIN: "PIN errado" and the dots shake (or appear without motion if animations are off). After 5 wrong tries, a 1-minute wait (owner, 2026-11-18), with **Esqueci o PIN** offered. |
| 3 | **Área dos pais** | Picks a section | Four rows: **Contatos** ("3 pessoas, 1 grupo"), **Horário de silêncio** ("Escola · Dormir"), **Conexão** ("Conectado ao WhatsApp de {perfil}"), **Sobre** (version, Check for updates, Ajuda). The area closes after 2 minutes idle (owner, 2026-11-18). |
| 4 | **Contatos** | Sees the allowed people and groups with photos | Lists them in the order the kid sees them. **+ Adicionar** is the one coral action. |
| 5 | **Adicionar** | Searches by name, taps one or more | Searches the parent's WhatsApp contacts and groups. Those already allowed show a check. A tap adds; a second tap removes. |
| 6 | **Remover** | Taps **Remover** on a row (a swipe also works) | Removes it at once with a **Desfazer** snackbar (5 s); no confirm. The conversation history stays on the tablet but is hidden from the kid. |
| 7 | **Contact detail** | Toggles **Pode falar no horário de silêncio** | The per-contact exception (owner's decision). Default off. |
| 8 | **Horário de silêncio** | Adds, edits or switches periods off | Each period has a name (Escola, Dormir…), days and start and end times. Suggested names come first; times are prefilled (Escola 7h–12h on weekdays, Dormir 21h–7h every day; owner, 2026-11-18). A switch pauses a period (e.g. holidays) without deleting it. |
| 9 | **Sair** | Taps **Sair**, or leaves it | Back to the kid home; the changes are live already. |

## Branches
| At step | If | Then |
|---|---|---|
| 2 | Forgot PIN | Flow "Parent setup + link", Forgot PIN. |
| 5 | WhatsApp not connected | The saved list shows. Adding is off, with "Conecte de novo para adicionar pessoas" and **Conectar** (goes to Conexão). |
| 5 | No results | "Ninguém com esse nome nos seus contatos do WhatsApp." The search text is kept. |
| 5 | A group is picked | The kid sees the group's name and photo, and **every member of the group can see what the kid sends.** The line under each group in the search says so before the tap: "Todos do grupo veem as mensagens." |
| 6 | Desfazer | Puts the contact back where it was. |
| 6 | Removing the last contact | Allowed. The kid's home shows its empty state. No confirm (undo is there). |
| 7 | A message arrives from a contact during quiet hours (not an exception) | It's kept and shown to the kid when the period ends (flow "Kid receives messages"). |
| 8 | Periods overlap | Allowed; quiet means any period is on. The list shows "Escola e Dormir se sobrepõem às 7h" as a plain note. |
| 8 | End before start (21h–7h) | Read as overnight; the row says "até 7h do dia seguinte". |
| any | Back | Returns one level; nothing is lost (each change saves at once). |

## States per step
| Step | Empty | Loading | Error | Success | Offline |
|---|---|---|---|---|---|
| Contatos | "Ninguém ainda. Adicione quem {criança} pode chamar." with **+ Adicionar** | — | — | List | List works; adding off |
| Adicionar | Before typing: the parent's recent WhatsApp chats first | Loading contacts | "Não deu para carregar seus contatos. Tentar de novo" | Check on the picked ones | Off, with Conectar |
| Horário de silêncio | "Sem horário de silêncio. {criança} pode mandar mensagens a qualquer hora." **+ Adicionar período** | — | Invalid time is prevented by the time picker | List with switches | Works (all on the tablet) |

## Exits
- **Sair** or idle goes back to the kid home, with every change already saved.
- From **Conexão**: reconnect (flow "Parent setup + link").

## Remembers
- The last section opened in the parent area.
- Recent WhatsApp chats lead the add list.
- Paused periods keep their times.

## Notes
- **Product law:** the allowed list is the only list. Oiê never shows the kid anyone else, and incoming messages from anyone else are never shown to the kid. They stay in the parent's WhatsApp as usual.
- **Groups (owner, 2026-11-18):** allowed, with the warning that everyone in the group sees the kid's messages. A family group is a common real case.
- **Data:** the list and the periods live on the tablet only.

## Flow review (ux-user-flows §13)
| # | Question | Result |
|---|---|---|
| 1 | Job | ✅ One actor, one job, measured by time and the product-law test. |
| 2 | Entries | ✅ Setup, the Pais button, the kid's request (proposed). |
| 3 | Happy path | ✅ Add is 3 taps after the PIN. |
| 4 | Branches | ✅ Above. |
| 5 | States | ✅ Above. |
| 6 | Errors | ✅ Prevented by the time picker and the group warning before the tap; search text kept. |
| 7 | Control | ✅ Undo for removing, no confirms; back keeps everything. |
| 8 | Progress and resume | ✅ Each change saves at once; nothing multi-step to resume. |
| 9 | Memory | ✅ Recent chats lead the search. |
| 10 | Exits | ✅ Back to the kid home. |
| 11 | Conventions | ✅ Android time pickers and switches; swipe to remove has a button too. |
| 12 | Speed | ✅ Local; contact loading shows progress. |
| 13 | Accessibility | ✅ Swipe has a button alternative (2.5.7); switches have labels; the PIN pad is 48dp keys. |
| 14 | Ethics | ✅ The group warning is said before adding. The kid can see the Pais button exists: there's no hidden surveillance. |
| 15 | Evidence | 🟡 Test: do parents understand the group warning and the per-contact exception? |

**Issues**
- **Important:** the per-contact exception sits inside each contact's detail, so it's easy to miss. Fix: in Horário de silêncio, add a row "Podem falar mesmo assim: Mãe, Vovó" that opens the list of switches.
- **Polish:** the 5-try PIN wait (proposed) needs owner confirmation.
