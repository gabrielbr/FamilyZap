# Oiê tablet prototype

`index.html` holds 18 artboards: 8 screens at 1280×800 (landscape), each in light and dark, plus the kid home and the conversation in portrait (800×1280). Everything on them comes from:
- `design/generated/tokens.css`;
- the design system's line icons;
- the logo files.

Names are examples, and the QR code is a fake one labelled "Exemplo". The rendered PNGs are in `screens/`; `OIE_2026_Prototype-overview_16x9_01.png` shows them all at once.

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

**To re-render:** open `index.html` in a browser. These are design references; the app itself is built in Jetpack Compose from `design/generated/OTokens.kt`.

**Not drawn yet:**
- the PIN pad;
- setup steps 3–6 (names, PIN, first contacts, quiet hours) and the Done screen;
- the add-contact search;
- recording in progress;
- the photo preview;
- the sticker tray;
- the undo snackbar;
- the error and offline states.
