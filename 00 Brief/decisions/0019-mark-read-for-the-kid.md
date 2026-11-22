# 0019 · What reaches the kid is marked read on the parent's WhatsApp

- **Date:** 2026-11-22
- **Decided by:** the owner: "Messages sent to the kid can be marked as read on the parent's device? So it avoids bombarding the parent with notifications that are not for them." Asked when, they chose "as soon as it arrives", on by default, with a switch in Ajustes.
- **Before:** rule L7 sent no read receipts from chats. The only exception was the statuses Tito watched (v0.36). Everything for Tito stayed unread on the parent's phone.
- **Decision:**
  - A message that reached the kid is marked read on the parent's WhatsApp a moment after it arrives. That means the message rule said notify, or quiet in quiet hours (`ReadMark`, `MarkChatRead`).
  - What stays with the parent is never marked.
  - Never on the line to the parent or the parent's own chat; never on a broadcast.
- **What it costs:** the sender sees the blue ticks before Tito opens it. The switch says so.
- **Risk to the number:** low. Read receipts are what every WhatsApp sends: one receipt per message, no new messages. Unlike the unread mark (v0.41, off by default), it's normal client behaviour.
- **Rule L7, reworded** (`docs/whatsapp-link.md`): no automatic "online" or "typing". Read receipts go only for the statuses Tito watched (v0.36) and for the messages that reached him (v0.55, while the switch is on).
- **Revisit if:** families say the early blue ticks mislead senders. Then consider "when Tito reads it" (the other option the owner was shown).
