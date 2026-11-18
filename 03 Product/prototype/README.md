# Oiê tablet prototype

`index.html` is built by `build.py` (run `python3 "03 Product/prototype/build.py"`). It holds 44 artboards: 21 screens at 1280×800 in light and dark, plus the kid home and the conversation in portrait. Everything comes from:
- `design/generated/tokens.css`;
- the core line icons in `design/core/icons/`;
- the logo and the stickers in `01 Brand/`;
- `components.css` (built only from `var(--gds-*)`).

Names are examples, and the QR code is fake ("Exemplo"). The rendered PNGs are in `screens/`.

| # | Screen | Flow |
|---|---|---|
| 01 | Kid home | Kid sends a message |
| 02 | Kid home in quiet hours | Contacts + quiet hours |
| 03 | Conversation | Kid sends / receives |
| 04 | Kid notice (first open) | Kid sends a message |
| 05 | Setup 1: risk notice | Parent setup + link |
| 06 | Setup 2: link by QR | Parent setup + link |
| 07 | Parent area: contacts | Contacts + quiet hours |
| 08 | Parent area: quiet hours | Contacts + quiet hours |
| 09 | PIN pad (parent area) | Contacts + quiet hours |
| 10 | Setup 3: names | Parent setup + link |
| 11 | Setup 4: create PIN | Parent setup + link |
| 12 | Setup 5: first contacts | Parent setup + link |
| 13 | Setup 6: quiet hours | Parent setup + link |
| 14 | Setup done | Parent setup + link |
| 15 | Parent area: add contact (search) | Contacts + quiet hours |
| 16 | Recording a voice message | Kid sends a message |
| 17 | Photo preview | Kid sends a message |
| 18 | Sticker tray | Kid sends a message |
| 19 | Sending, with undo | Kid sends a message |
| 20 | Offline | Kid sends / receives |
| 21 | Kid home, link dropped | Parent setup + link (reconnect) |

**Not drawn:** Ajuda (help) and About in the parent area (audit I-7); the 2-minute PIN wait.
