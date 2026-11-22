# Flow: The parent area's structure (v0.44)

_Designed with `ux-user-flows` on 2026-11-21, at the owner's request: "in the parent area, settings is mixed with help and everything is crowded there. Do a careful analysis on how to break down the parent section."_

- **Actor and job story:** the parent, on the kid's tablet after the PIN, usually in a hurry with the kid nearby. *When I open the parent area to change one thing (add a person, turn on quiet, fix the connection, rename, read an answer), I want to see at once where it lives and find only that there, so I can do it and hand the tablet back.*
- **Success measures:**
  - every task is found in one tap from the area's first screen;
  - no section mixes changing things with reading about them;
  - no section's resting state needs more than about two tablet screens of scrolling.
- **Entry points:** the PIN, unchanged; "Conectar o WhatsApp" back from pairing (it lands in Ajustes); "Ver o relatório" from the connection's crash note (it lands in Ajuda).

## What was wrong (inventory, before)
| Section | What it held | Problem |
|---|---|---|
| Contatos | People and groups, the parent line card, add, sync | Fine: one job. |
| Horário de silêncio | Periods, hand-set quiet, who can talk anyway | Fine: one job. |
| Conexão | Status, connect, the crash note, unlink | A short page for a rare job, though connecting matters when it's down. |
| **Ajustes e ajuda** | **4 name fields with a preview, Bolha, Lembrete, Novidades (with posting), the unread mark, 11 help questions, Problemas, Sobre (version, updates, notices, credits)** | **Three jobs in one scroll:** changing settings, learning, and upkeep. That's 8 headings over about 5 tablet screens. The name fields alone fill the first screen, though they're set once in setup and rarely touched again. |

## Card sort (by the parent's task and how often it comes up)
| Item | Task | How often | New home |
|---|---|---|---|
| Contacts, parent line | Who Tito talks to | Weekly | **Contatos** (unchanged) |
| Quiet periods, hand-set quiet | When | Weekly | **Silêncio** (unchanged) |
| Connection status, connect, unlink | The WhatsApp link | When it drops | **Ajustes**, first, so a dropped link is the first thing seen there |
| Unread mark (your line) | How Oiê behaves in your WhatsApp | Rare | **Ajustes**, under Conexão (it's about the parent's WhatsApp) |
| Names, nicknames, emoji | Who is who | Once, then rarely | **Ajustes**, as one summary row that opens **Nomes** (one level down) |
| Bolha, Lembrete, Novidades | What the tablet does | Rare | **Ajustes** |
| Help questions | Learning | When unsure | **Ajuda** |
| Problems report | Upkeep | After a crash | **Ajuda** (first while there's a report, as before) |
| Version, updates, notices, credits | Upkeep | Rare | **Ajuda › Sobre** |

The four sections stay four, so the phone's 2 × 2 grid needs no new row. "Conexão" stops being a tab of its own: what it held is a few lines, and it now leads Ajustes. "Ajuda" gets a tab of its own.

```mermaid
flowchart TD
  pin([PIN]) --> contatos[Contatos]
  contatos <--> silencio[Silêncio]
  contatos <--> ajustes[Ajustes]
  contatos <--> ajuda[Ajuda]
  ajustes --> conexao[Conexão: status, Conectar / Desconectar,<br/>marcar como não lida]
  ajustes --> nomesRow[Nomes: 'Ana · Tito (Chico) · 😎' ›]
  nomesRow -- tap --> nomes[Nomes: four fields + the preview]
  nomes -- Voltar --> ajustes
  ajustes --> extras[Bolha · Lembrete · Novidades]
  conexao -- 'Ver o relatório' --> ajuda
  ajuda --> perguntas[Perguntas] --> problemas[Problemas] --> sobre[Sobre: versão, atualizações, avisos]
  pair([Back from pairing]) --> ajustes
```

## Steps
| # | Screen | What the parent does | What the system does |
|---|---|---|---|
| 1 | **Tabs** | Picks Contatos, Silêncio, Ajustes or Ajuda | Tablet: the list on the left, as before, with Ajustes (gear) and Ajuda (info) replacing Conexão and Ajustes e ajuda. Phone: the same 2 × 2 grid. |
| 2 | **Ajustes** | Reads the connection; turns things on or off | In order: Conexão (with the unread mark under it, once the parent's line exists), then the Nomes row, then Bolha, Lembrete and Novidades. |
| 3 | **Nomes** | Taps the row, then edits | The four fields and the preview, under a bar with "Voltar" and "Nomes". Changes save as they're typed (as before); Voltar returns to Ajustes. The row shows the result: "Ana · Tito (Chico, Fran) · 😎". |
| 4 | **Ajuda** | Opens a question; reads the report; checks the version | Perguntas (the accordion), Problemas, Sobre. After a crash, Problemas comes first (as before). |

## Branches
| At | If | Then |
|---|---|---|
| 2 | The link is down | Conexão leads Ajustes with "Conectar o WhatsApp". |
| 2 | Not linked to a real WhatsApp | The Conexão lines say so (as the Conexão tab did). |
| 2 | No parent line yet | The unread mark is hidden (as before). |
| 3 | A field is left blank | The last name is kept (as before). |
| 3 | The area times out (2 min) | Back to the kid's home (as before). Nothing is lost, because fields save as typed. |
| Any | Someone was looking for "Ajustes e ajuda" | Updated help lines point to "Ajustes › …" and "Ajuda". |

## States
Unchanged per component: Conexão's connecting, connected and down states; the switches; the report's empty and filled states; the undo snackbar.

## Exits
"Sair" in the bar, from every section; the 2-minute timeout.

## Taps on the common paths
| Task | Before | After |
|---|---|---|
| Add a contact | 0 (first tab) | 0 |
| Turn quiet on | 1 | 1 |
| Reconnect | 1 (Conexão) | 1 (Ajustes, first block) |
| Change a nickname | 1, then scroll | 2 (Ajustes, Nomes), no scroll. This is rare (less-is-more Q5), so one more tap is fine. |
| Turn on Novidades | 1, then about 1 screen of scroll | 1, then a short scroll |
| Read a help answer | 1, then about 2 screens of scroll | 1 (Ajuda), the first block |

## Flow review (ux-user-flows §13)
| # | Question | Answer |
|---|---|---|
| 1 | Job | ✅ One actor, one job, three measures. |
| 2 | Entries | ✅ PIN; back from pairing to Ajustes; the crash note to Ajuda. The route numbers keep their meaning (2 = where the connection is, 3 = where the report is). |
| 3 | Happy path | ✅ No common task gets longer. |
| 4 | Branches | ✅ |
| 5 | States | ✅ Unchanged components. |
| 6 | Errors | ✅ |
| 7 | Control | ✅ Voltar from Nomes; Sair everywhere. |
| 9 | Memory | ✅ The Nomes row shows the current names, so they don't have to be remembered. |
| 10 | Exits | ✅ |
| 11 | Conventions | ✅ Settings and Help as separate places; the connection inside Settings, as in Android's own settings. |
| 13 | Accessibility | ✅ The tabs keep `Role.Tab` and their selected state. The Nomes row is a button with its value read out. |
| 14 | Ethics | ✅ Nothing hidden: the help and the risk text are one tap away, in Ajuda. |
| 15 | Evidence | 🟡 Watch the owner find "apelidos", "Novidades" and "versão" on the tablet. |

## v0.52: Material navigation (decision 0018)
- **Same four sections:** Contatos, Silêncio, Ajustes, Ajuda.
- **Tablet:** Material's navigation rail on the left.
- **Phone:** Material's navigation bar at the bottom, the thumb's reach (it was a 2 × 2 grid of tabs on top).
- **Undo:** on a phone, the undo snackbar sits above the bar.

**Review (§13):**
- **Conventions ✅:** Android's standard top-level navigation.
- **Speed ✅:** one tap to any section, as before.

