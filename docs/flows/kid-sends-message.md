# Flow: Kid sends a message

_Stage 5. Designed with `ux-user-flows` on 2026-11-18. The owner made these decisions on 2026-11-18:_
- _during quiet hours the kid can't send;_
- _photos come from the camera or the gallery;_
- _stickers are an Oiê set;_
- _the kid can undo a send for 5 s._

- **Actor and job story:** a kid aged 9–12 on the family's Android tablet. *When I miss someone in my family or have something to tell them, I want to send them a message, a voice message, a photo or a sticker in a couple of taps, so I can feel close to them without asking for a parent's phone.*
- **Success measure:** the kid sends at least 3 messages a week to family, unprompted (PRODUCT.md). Per send: time from opening Oiê to "Enviada" under 10 s for a voice message.
- **Entry points:**
  1. The Oiê icon on the tablet, which opens the kid's home.
  2. A family member's notification, which opens their conversation (flow "Kid receives messages").
  3. Returning mid-task: an unsent draft or recording is still there.
- **Preconditions:**
  - the parent has set up and linked their WhatsApp (flow "Parent setup + link");
  - at least one contact is allowed (flow "Choose contacts + quiet hours").
- **Taps on the common path:** a voice message to grandma takes **2 taps and a hold**: tap Vovó, hold to record, release to send. A text takes tap, type, then tap Enviar.

```mermaid
flowchart TD
  start([Open Oiê on the tablet]) --> first{First time?}
  first -- Yes --> notice[Kid notice, once:<br/>'Suas mensagens saem do WhatsApp de {nome}…'<br/>button: Entendi] --> home
  first -- No --> home[Kid home: allowed family as big photos]
  home --> quiet{Quiet hours now?}
  quiet -- Yes --> quietHome[Home shows 'Horário de silêncio até 18h'<br/>photos visible, can read, can't send]
  quiet -- No --> pick[Tap a family member]
  pick --> convo[Conversation with them]
  convo --> kind{What to send}
  kind -- Hold mic --> rec[Recording, timer shown] -- Release --> pending
  rec -- Slide away or tap Cancelar --> convo
  kind -- Type --> text[Text field] -- Enviar --> pending
  kind -- Photo --> photo[Camera or gallery] --> preview[Preview, Enviar / Trocar] -- Enviar --> pending
  kind -- Sticker --> sticker[Oiê sticker tray] -- tap one --> pending
  pending[In the chat as 'Enviando…'<br/>snackbar 'Desfazer' 5 s] -- Desfazer --> convo
  pending -- 5 s pass --> sent{Link to WhatsApp OK?}
  sent -- Yes --> done([Shows 'Enviada' with the time])
  sent -- No, offline or unlinked --> queued[Kept, 'Vai quando a internet voltar'<br/>or 'Peça para {nome} conectar de novo']
```

## Steps
| # | Screen / step | What the kid does | What the system does |
|---|---|---|---|
| 0 | **Kid notice** (first open only) | Reads it, taps **Entendi** | Shows once: "Suas mensagens saem do WhatsApp de {nome}, então {nome} pode ver o que você manda." Not shown again (it can be found in Ajuda). |
| 1 | **Kid home** | Sees the allowed family as 96dp round photos with names. Taps one | Shows only contacts the parent allowed, and the people the kid talked to most recently first. A dot with a word ("2 novas") marks unread messages. |
| 2 | **Conversation** | Sees recent messages. Picks how to answer | Opens on the latest message. The 72dp record button is the one accent. The text field, photo and sticker sit beside it as quiet buttons. |
| 3a | **Voice message** | Holds the record button and talks, then releases | Records with a running timer and a waveform. Releasing sends it. Sliding away or tapping **Cancelar** throws it away. **Tap-to-record** is also offered: tap starts, tap again sends (no drag needed, WCAG 2.5.7). Maximum 2 minutes, with a visible countdown in the last 10 s. |
| 3b | **Text** | Types, taps **Enviar** | **Enviar** becomes active when the text isn't empty. The draft is kept if the kid leaves. |
| 3c | **Photo** | Taps the photo button, picks **Câmera** or **Galeria**, then **Enviar** | Asks for the camera or photos permission the first time, with a reason in plain words. Shows a big preview with **Enviar** and **Trocar**. |
| 3d | **Sticker** | Taps the sticker button, taps a sticker | Opens the Oiê set as a sheet. One tap sends. |
| 4 | **Sending** | Can tap **Desfazer** within 5 s | The message shows in the chat as "Enviando…", with a **Desfazer** snackbar for 5 s. Nothing leaves the tablet until the 5 s are up. |
| 5 | **Sent** | — | Goes out through the parent's WhatsApp. "Enviada · 16h32" shows under the **latest** sent message only (less-is-more: a state is said once). The family sees it from the parent's number, tagged with the kid's name, e.g. "Tito: Saudade, vó". |

## Branches
| At step | If | Then |
|---|---|---|
| 1 | Quiet hours are on | The home shows "Horário de silêncio até 18h" at the top. Photos stay, and the kid can open conversations and read and listen, but the send controls are **off**, with the same line in place of the record button. No queueing (owner's decision). |
| 1 | No allowed contacts | Empty state: one cluster of the logo's rounds, "Ainda não tem ninguém aqui. Peça para {nome} escolher quem você pode chamar." No button: the kid can't fix it. |
| 2 | Back / system back | Returns to the home. Any draft or photo choice is kept. |
| 3a | Recording under 1 s | Treated as a slip and not sent. Line: "Segure o botão enquanto fala." |
| 3a | Microphone permission denied | "O Oiê precisa do microfone para gravar áudio." **Abrir configurações**. Text, photo and sticker still work. |
| 3c | Camera or photos permission denied | The same pattern, for that source only. The other source still works. |
| 4 | **Desfazer** | The message is removed and the recording or text goes back into the composer, so nothing is lost. |
| 5 | Offline | The message is kept: "Vai quando a internet voltar". It goes out on its own when the connection returns. |
| 5 | The link to WhatsApp dropped (the parent unlinked, or WhatsApp logged it out) | The message is kept: "Peça para {nome} conectar de novo." The parent's phone gets the reconnect prompt (parent flow). No tech words. |
| 5 | WhatsApp refused it (e.g. the number is restricted) | "Não foi. Tente de novo mais tarde." **Tentar de novo**. The parent sees the real reason in their part of the app. |
| any | Quiet hours start while the kid is writing | The current message can still go if it was already sent. New sends turn off, and the draft is kept for later. |

## States per step
| Step | Empty | Loading | Partial | Error | Success | Offline |
|---|---|---|---|---|---|---|
| Home | "Ainda não tem ninguém aqui…" | Photos as initials on cream until they load | Some photos missing: show initials | — | Allowed family shown | Home works; the banner reads "Sem internet. Suas mensagens vão quando voltar." |
| Conversation | "Mande o primeiro oi para {Vovó}." | Skeleton rows under 1 s | Older messages load on scroll | Media failed: "Não abriu. Toque para tentar de novo." | — | Read what's on the tablet; sends are queued |
| Sending | — | "Enviando…" | — | "Não foi…" + Tentar de novo | "Enviada · hora" | "Vai quando a internet voltar" |

## Exits
- **Sent:** stays in the conversation, ready for the reply. Back goes to the home.
- **Undo:** back in the composer with the content.
- **Quiet hours:** the home, read-only, with the end time.
- **Errors:** the message is never lost; it stays in the chat with its state and a retry.

## Remembers
- The draft text, per contact.
- The last photo source (Câmera or Galeria) opens first next time.
- The home orders family by most recent conversation.

## Notes
- **Copy** follows `01 Brand/voice.md`: Enviar, Desfazer, Cancelar, Câmera, Galeria, Trocar, Entendi.
- **Data:** messages go out from the tablet through the parent's WhatsApp link. Nothing is stored on our servers.
- **Product law:** the kid can only reach allowed contacts. No search, no new numbers, no forwarding to people outside the list.
- **Open:** the Oiê sticker set still has to be drawn (Stage 5 assets); the 2-minute limit for voice messages is an assumption to test.

## Flow review (ux-user-flows §13)
| # | Question | Result |
|---|---|---|
| 1 | Job | ✅ One actor (the kid), one job story, success measured per week and per send. |
| 2 | Entries | ✅ App icon, notification, returning mid-task. A deep link from the parent's side is not needed in v1. |
| 3 | Happy path | ✅ Voice: tap, hold, release. Nothing is asked before it's needed. Permissions are asked in context. |
| 4 | Branches | ✅ Cancel, back, undo, permissions, offline, unlinked, refused, quiet hours start mid-task. |
| 5 | States | ✅ Table above. |
| 6 | Errors | ✅ Prevented where possible (a too-short recording isn't sent). Inline and plain, content kept, retry offered. The parent gets the real reason. |
| 7 | Control | ✅ Back everywhere; **undo over confirm** (5 s). |
| 8 | Progress and resume | ✅ Drafts are kept per contact; sending state is shown. |
| 9 | Memory | ✅ Nothing to remember across steps; nothing is asked twice. |
| 10 | Exits | ✅ No dead ends. The empty home tells the kid who can fix it (the parent) without a useless button. |
| 11 | Conventions | ✅ Hold-to-record and preview-before-send are familiar from messaging apps, without the WhatsApp look. |
| 12 | Speed | 🟡 Undo delays sending by 5 s by design. The "Enviando…" state is shown at once (0.1 s). The link's real send time is unknown until tested. |
| 13 | Accessibility | ✅ Tap-to-record as the alternative to holding (2.5.7). Targets 48dp or more, with the record button at 72dp. Labels on every icon. States in words, not colour alone. |
| 14 | Ethics | ✅ The kid is told once, plainly, that the parent can see what they send. No streaks, no nagging, no "send more" prompts. |
| 15 | Evidence | 🟡 Assumptions to test with 3–5 kids aged 9–12: hold vs tap to record, the 5 s undo, and whether the kid notice is understood. |

**Issues**
- **Important:** quiet hours with "can't send" may frustrate a kid who wants to say good night at bedtime. Fix to consider: let the parent allow some contacts during quiet hours, a per-contact exception. Decide in the contacts and quiet hours flow.
- **Polish:** the record button's label must change with the state (Gravar → Gravando 0:12 → Solte para enviar) for screen readers.
