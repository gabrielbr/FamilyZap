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
| 1 | **Kid home** | Sees the allowed family (v0.38: rows on a phone, cards on the tablet, each with the last message; flow kid-home-and-attach.md). Taps one | Shows only contacts the parent allowed, in the order the parent set them (v0.34: a steady order, so the kid finds each person in the same place every day). A dot with a word ("2 novas") marks unread messages. |
| 2 | **Conversation** | Sees recent messages. Picks how to answer | Opens on the first unread message under a "Novas" line, else on the newest (v0.34). The 72dp record button is the one accent. The text field, photo and sticker sit beside it as quiet buttons. |
| 3a | **Voice message** | Holds the record button and talks, then releases | Records with a running clock ("Gravando · 0:05"). Releasing sends it. Sliding off the button and releasing throws it away ("Solte para cancelar" while off). **Cancelar** shows beside the clock. **Tap-to-record** is TalkBack's action on the button: tap starts, tap again sends (no hold or drag needed, WCAG 2.5.7); a quick touch tap is a slip (below). Maximum 2 minutes: the last 10 s count down ("Termina em 8 s"), then it goes by itself. (No waveform: the clock says it's recording, v0.34.) |
| 3b | **Text** | Types, taps **Enviar** | **Enviar** becomes active when the text isn't empty. The draft is kept if the kid leaves. |
| 3c | **Photo** | Taps the photo button (v0.38: **Anexar**), picks **Câmera** or **Galeria**, then **Enviar** | Asks for the camera or photos permission the first time, with a reason in plain words. Shows a big preview with **Enviar** and **Trocar**. |
| 3d | **Sticker** | Taps the sticker button (v0.38: **Anexar**, stickers below the sources), taps a sticker | Opens the Oiê set as a sheet. One tap sends. |
| 4 | **Sending** | Can tap **Desfazer** within 5 s | The message shows in the chat as "Enviando…", with a **Desfazer** snackbar for 5 s. Nothing leaves the tablet until the 5 s are up. |
| 5 | **Sent** | — | Goes out through the parent's WhatsApp. "Enviada · 16h32" shows under the **latest** sent message only (less-is-more: a state is said once). The family sees it from the parent's number, tagged with the kid's name, e.g. "Tito: Saudade, vó" (since v0.22: in bold, after the emoji the parent chose: "😎 **Tito**: Saudade, vó"). |

## v0.11: real voice
Recording is real: Opus in Ogg on Android 10 and newer (WhatsApp's own voice-note format), AAC before that. The recording is sealed the moment it stops and goes out as a WhatsApp voice note (PTT). The microphone permission is asked at the end of setup, with the parent, or at the first recording on an older setup. If recording can't start: "Não deu para gravar. Tente de novo."

## Branches
| At step | If | Then |
|---|---|---|
| 1 | Quiet hours are on | The home shows "Horário de silêncio até 18h" at the top. Photos stay, and the kid can open conversations and read and listen, but the send controls are **off**, with the same line in place of the record button. No queueing (owner's decision). |
| 1 | No allowed contacts | Empty state: one cluster of the logo's rounds, "Ainda não tem ninguém aqui. Peça para {nome} escolher quem você pode chamar." One button, **Pedir para adicionar alguém**, which tells the parent in their area (the owner's choice, 2026-11-18). |
| 2 | Back / system back | Returns to the home. Any draft or photo choice is kept. |
| 3a | Recording under 1 s | Treated as a slip and not sent. Line: "Segure o botão enquanto fala." |
| 3a | Microphone permission denied | "O Oiê precisa do microfone para gravar áudio." **Abrir configurações**. Text, photo and sticker still work. |
| 3c | Camera or photos permission denied | The same pattern, for that source only. The other source still works. |
| 4 | **Desfazer** | The message is removed and the recording or text goes back into the composer, so nothing is lost. |
| 5 | Offline | The message is kept: "Vai quando a internet voltar". It goes out on its own when the connection returns. |
| 5 | The link to WhatsApp dropped (the parent unlinked, or WhatsApp logged it out) | The message is kept: "Peça para {nome} conectar de novo." The parent reconnects from the parent area on the tablet (flow "Parent setup + link"). No tech words. |
| 5 | WhatsApp refused it (e.g. the number is restricted) | "Não foi" under the message and **Tentar de novo** (v0.8). One automatic retry happens first, only after the link comes back (rule L4). The parent sees the real reason in their part of the app. |
| any | Quiet hours start while the kid is writing | The current message can still go if it was already sent. New sends turn off, and the draft is kept for later. |

## States per step
| Step | Empty | Loading | Partial | Error | Success | Offline |
|---|---|---|---|---|---|---|
| Home | "Ainda não tem ninguém aqui…" | Photos as initials on cream until they load | Some photos missing: show initials | — | Allowed family shown | Home works; the link banner says it ("Peça para {nome} conectar de novo."), and messages wait to go out. |
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
- The home keeps the parent's order (v0.34; the earlier plan was most recent first, dropped so places don't move).

## Notes
- **Copy** follows `01 Brand/voice.md`: Enviar, Desfazer, Cancelar, Câmera, Galeria, Trocar, Entendi.
- **Data:** messages go out from the tablet through the parent's WhatsApp link. Oiê has no servers; nothing reaches us.
- **Product law:** the kid can only reach allowed contacts. No search, no new numbers, no forwarding to people outside the list.
- **Open:** the Oiê sticker set still has to be drawn (Stage 5 assets); the 2-minute limit for voice messages is an assumption to test.

## v0.22: the name in bold, with an emoji
_The owner, 2026-11-19: "the name of Tito when sent to WhatsApp should be in bold, and the parent could choose an emoji, so readability would be better", e.g. `😎*Tito*: (message)`._

- **What the family sees:** WhatsApp's own bold: `*Tito*` shows as **Tito**. With an emoji: 😎**Tito**: Saudade, vó. A photo's or sticker's caption is the name alone, the same way (😎**Tito**).
- **Where the parent chooses:** Ajustes e ajuda › Nomes › **Emoji antes do nome**, a field that takes one emoji from the keyboard. It's optional; empty means just the bold name.
- **What it shows:** a line under the field, "Fica assim no WhatsApp:", with a sample message rendered the way WhatsApp will show it. So the parent sees the result without sending anything.
- **Rules:**
  - only one emoji is kept (the first); letters and numbers aren't an emoji and are ignored;
  - a star (*) in the kid's name is dropped, so the bold can't break;
  - the emoji is the parent's content, sent to WhatsApp. Oiê's own interface still uses no emoji: the field shows what the parent typed, nothing more.
- **Nothing else changes:** the message rule reads the family's replies, not the kid's signature. The kid's screen shows the kid's own messages without the signature, as before.

| # | The parent | The system |
|---|---|---|
| 1 | Opens Ajustes e ajuda › Nomes | Sees "Emoji antes do nome" (empty) and "Fica assim no WhatsApp: **Tito**: Oi, vó". |
| 2 | Taps the field, picks 😎 on the keyboard | The preview shows 😎**Tito**: Oi, vó at once; saved. |
| 3 | Types more, or a letter | Only the first emoji is kept; a letter alone is ignored and the preview shows no emoji. |
| 4 | Clears the field | Back to the bold name alone. |

## v0.39: the name on every kind of message
_The owner, 2026-11-21: "Every type of message should have the kid prefix (emoji+name). Right now áudio, stickers and possible others miss that."_

- **The gap:** text carries the name in front, and photos, videos, GIFs and Oiê's own stickers carry it as their caption. WhatsApp gives a **voice note** and a **real sticker** (a family sticker the kid saved) no caption, so those arrived with no name.
- **The fix:** before one of those, the name goes alone on its own line, `😎*Tito*` (shown as 😎**Tito**), and the audio or sticker follows. Same bold and emoji as v0.22.
- **Once per burst** (the owner's choice): the name line goes only if the name didn't reach that chat in the last 2 minutes. Five audios in a row: one name line. Any message that carries the name (a text, a photo…) counts.
- **Reactions stay bare** (the owner's choice): a reaction is a mark on a message, not a message, and can't carry a name.
- **What the kid sees:** nothing changes on the tablet. The name line is Oiê's own, and its echo is ignored like every send.
- **Pace and failures:** the name line is a send like any other (rule L3: 2 s apart, at most 30 in 10 minutes). If it can't go, the audio or sticker isn't sent either, and the kid gets the usual "Não foi" with Tentar de novo. The retry sends the name again if 2 minutes have passed.

| # | The kid | What the family sees in WhatsApp |
|---|---|---|
| 1 | Records an audio to Vovó (nothing sent to her in a while) | 😎**Tito**, then the audio, 2 s later |
| 2 | Sends a family sticker a minute later | The sticker alone (the name went a minute ago) |
| 3 | Writes "Te amo" | 😎**Tito**: Te amo |
| 4 | Sends an audio 5 minutes later | 😎**Tito**, then the audio |

## v0.23: photos work
_The owner's report (2026-11-19, with a photo of the screen and issue #34): every photo, from the camera or the gallery, showed "Não deu para abrir essa foto. Tente outra."_

- **Cause:** a bug since v0.1. Oiê first reads only a photo's size, and Android's answer to that step is empty on purpose (the size comes back on the side). Oiê took the empty answer for "can't open", so it refused every photo. The tests used sample data, so it showed only on a real tablet.
- **Now:**
  - photos are accepted and scaled to at most 1600 px, as designed;
  - a camera photo is turned upright: the camera stores the turn in the file's metadata, which Oiê drops on re-encoding (for privacy), so Oiê applies it first;
  - when a photo really can't be read (not a picture, a format Android can't open), the same message shows, and the problem report now says why (the type, or the error), with no names and no picture.
- **The message stays:** "Não deu para abrir essa foto. Tente outra." It's still true for a file that isn't a picture.

## v0.28: sending videos and GIFs
_The owner, 2026-11-19: the kid can send videos up to **3 minutes** (from the gallery or recorded with the camera), and **GIF files** from the gallery. Big videos are shrunk with Google's Media3 library (Apache 2.0, decision 0006)._

| # | Screen | The kid | The system |
|---|---|---|---|
| 1 | Chat | Taps the camera button | The sheet "Foto ou vídeo": **Tirar foto**, **Gravar vídeo**, **Galeria** (the last one used comes first). |
| 2a | Camera app | Records a video | Android's camera, limited to 3 minutes. |
| 2b | Gallery | Picks a photo, a video or a GIF | Android's own picker, photos and videos. |
| 3 | "Vídeo para Vovó" | Waits a moment | "Preparando o vídeo…" with a bar: the video is shrunk to 720p on the tablet (or a GIF turned into WhatsApp's GIF form). Then its first frame and length, **Trocar** and **Enviar** (coral). |
| 4 | Same | Taps Enviar | Back to the chat with the 5-second undo, as for photos. It goes out signed **Tito**. |

### Limits and errors (said in words, never only by colour)
| If | Then |
|---|---|
| The video is longer than 3 minutes | "Esse vídeo tem mais de 3 minutos. Escolha um mais curto." It's not prepared. |
| Even shrunk, it's over 64 MB | "Esse vídeo ficou grande demais. Escolha um mais curto." |
| Android can't read the file | The photo message's twin: "Não deu para abrir esse vídeo. Tente outro." The report says why. |
| The kid backs out while preparing | The work stops and nothing is kept. |

### How a GIF goes
WhatsApp shows a GIF as a short silent video that loops. Oiê turns the GIF file into that form on the tablet (no GIF search service: nothing about the kid leaves the tablet), and it arrives in the family's WhatsApp as a GIF.

### Notes
- **Storage:** the prepared file is sealed on the tablet like photos; the camera's temporary file is deleted.
- **Quiet hours, the allowed list, the pace (L3) and one retry (L4)** apply as for every send.

## v0.31: GIFs, stickers and pictures from the keyboard, or pasted
_The owner's test (2026-11-20): GIFs from Gboard, and pasted media, couldn't be put in the text field._

- **What works now:** in the chat's text field the kid can
  - pick a **GIF** or a **sticker** in Gboard (or another keyboard that offers them),
  - **paste** a picture, a GIF or a video copied elsewhere,
  - drag one in, on a tablet with split screen.
- **Where it goes:**

| Comes in as | Goes to | Sent as |
|---|---|---|
| A GIF | the GIF preview (v0.28): "Preparando…", then Enviar | WhatsApp's GIF (loops) |
| A picture or a keyboard sticker | the photo preview: Trocar / Enviar | a photo |
| A video | the video preview (v0.28) | a video (3-minute limit) |
| Text | the field, as before | text |

- **Nothing is sent without Enviar**, the same as for the camera and the gallery. Quiet hours and the allowed list apply.
- **Not supported** (says "Não deu para abrir…" like any unreadable file): anything that isn't a picture, a GIF or a video.

## v0.32: voice messages play for the family
_The owner's test (2026-11-20): the kid can record and listen in Oiê, but the family receives an empty or broken audio._

- **Cause:** Android records Opus in Ogg with its own Ogg writer. Oiê plays that file, but WhatsApp didn't.
- **Now:** before sending, the recording is re-packed into a standard Ogg Opus stream:
  - the same Opus audio, untouched;
  - clean OpusHead and OpusTags headers;
  - pages with correct positions and checksums;
  - the length taken from the audio itself.
  The family's WhatsApp gets the same kind of file its own voice notes are.
- **If a recording can't be read** as Ogg Opus, it isn't sent broken: it shows "Não enviada · Tentar de novo" and the report says why.
- **Nothing changes for the kid:** record, release, send.

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

## v0.34: the design pass
- **Recording:** a running clock ("Gravando · 0:05"). Releasing sends; sliding off the button and releasing cancels ("Solte para cancelar", in the error colour, while off). A press under a second shows "Segure o botão enquanto fala.".
- **Undo:** the design system's snackbar, with one action, **Desfazer**. For a voice message, Desfazer means it isn't sent (the recording goes).
- **Listen before it goes:** the ▶ on the voice message itself, while the 5 seconds wait (v0.33 had a second button in the bar; it duplicated the ▶).
- **The chat opens** on the first unread message under "Novas", else on the newest; sending brings the newest into view.
- **Reactions:** "Segure uma mensagem para reagir ou ouvir." shows under the chat until the kid has reacted once.
- **The home:** it scrolls; the photos line up with the greeting; both banners can show (link down and quiet hours); "Preciso de você" opens a sheet, and answers in a snackbar, with the minutes left when it's too soon.
- **People paused by the parent** leave the home without a word (the kid can't change it, and the parent's reasons are theirs).

## v0.34: one verb
The kid's side says **Mandar** everywhere (the voice's own examples: "Mandar oi"): the send button, "Mandando para Vovó" in the undo bar, "Mandada" under the last message. "Enviar" is left only in the parent's "Enviar para o GitHub".
