# Flow: ten family features (v0.33)

_Designed with `ux-user-flows` on 2026-11-20. The owner asked for the suggested features in one batch. Decided with the owner:_
- _"escrevendo…" is left out: it would show the parent online while the kid's chat is open;_
- _"Preciso de você" goes only to people the parent marks;_
- _the nudge is off until the parent turns it on._

Every feature keeps the product law: only allowed contacts, nothing to Oiê's servers (there are none), no tracking of the kid, no safety promises.

## 1. "Vovó viu" (receipts)
- WhatsApp reports when a sent message reached the phone (delivered) and when it was read.
- Under the kid's **latest** sent message, one line: "Enviada" → "Entregue" → "Vovó viu". In a group: "Alguém viu" (WhatsApp says that someone read it, not who).
- If the family turned read receipts off, it stays "Entregue". That's their choice, said nowhere.
- On the line to the parent (v0.30) there are no receipts: the parent reads with their own account, which WhatsApp doesn't report.

## 2. Reactions
- **The family's reactions** show under the message, as a small chip: "❤️ Vovó". Several reactions show side by side.
- **The kid reacts** with a long press on any message (TalkBack offers the same as its long-press action, "Reagir"): a row of six (❤️ 😂 😮 😢 👍 🙏). Tapping one sends it; tapping the same again removes it, as in WhatsApp.
- These emoji are message content, the family's own way to talk. Oiê's own interface still uses none (decision 0009).
- A family reaction shows quietly; it doesn't notify.

## 3. Birthdays
- In a contact's editor (parent area): **Aniversário**, the day and the month (no year: no age is kept).
- On the day, the kid's home shows one line at the top, "Hoje é aniversário da Vovó", and tapping it opens her chat.

## 4. The gentle nudge
- In Ajustes e ajuda: **Lembrar Tito de falar com a família**, off by default.
- It sits after the people on the home screen (audit: it must never push them down).
- When it's on, and a person hasn't heard from the kid for 7 days, the kid's home shows one line: "Faz uma semana que você não fala com o Vovô". It never shows more than one, and it never counts streaks.
  - Tapping opens the chat.
  - **Agora não** hides it for that person for 7 days.
- Groups and paused chats are never nudged.

## 5. Draw and send
- The camera button's sheet gets **Desenhar**.
- A drawing pad with five colours from the palette, a thicker and thinner line, Desfazer, and Limpar.
- **Enviar** goes through the photo preview; it's sent as a picture.

## 6. A sticker from a photo
- In the sticker sheet: **Criar figurinha**. The kid picks a photo; its centre becomes a round sticker, saved with the family stickers. It's sent like those (a real WhatsApp sticker).

## 7. Read aloud
- In the message's long-press menu: **Ouvir**. Android's own voice reads the text on the tablet. Nothing is sent anywhere.

## 8. Listen before sending
- After a voice message is recorded, the 5-second undo bar gets **Ouvir**. The kid hears it, then lets it go or taps Desfazer.
- No extra tap on the common path (less-is-more): releasing still sends.

## 9. "Preciso de você"
- In a contact's editor: **Recebe "Preciso de você"**. The line to the parent (v0.30) is a good choice; so is the other parent.
- When at least one person is marked, the kid's home shows a quiet button, **Preciso de você**.
- Tapping it asks once: "Mandar para Ana e Mãe?" with **Mandar** / **Cancelar**.
- What it sends: "Preciso de você.", signed like every message ("*Tito*: Preciso de você."), to each marked person, at most once every 5 minutes. It shows in each chat as the kid's message, with its receipt.
- It goes in quiet hours too: the parent chose these people for this.
- It's a way to reach the parents, not a safety service. Help says so.

## 10. Photos per person
- In a chat's header: **Fotos**. A grid of the photos, videos and GIFs in that chat. Tapping one opens it big; Back returns.

## Left out
- **"Escrevendo…/gravando…"**: it needs the parent online (the owner's choice).
- **Calls**: not possible through the linked-device route (decision 0001).

## Flow review (ux-user-flows §13)
| # | Question | Result |
|---|---|---|
| 1 | Job | ✅ Each feature has one actor and one job above. |
| 3 | Happy path | ✅ None adds a tap to sending, the common action; reactions and Ouvir are one long press away. |
| 4 | Branches | ✅ Receipts off; no one marked; nudge off; no photos yet; TalkBack (the long-press action). |
| 7 | Control | ✅ Undo, Agora não, Cancelar, Desfazer. |
| 13 | Accessibility | ✅ Long press has a visible alternative; chips and the receipt line are text. |
| 14 | Ethics | ✅ No streaks, no pressure, no ages kept, no online status, no safety claims. |
