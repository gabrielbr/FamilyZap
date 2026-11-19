# Flow: Problem report

_Designed with `ux-user-flows` on 2026-11-19 (v0.14), after the owner's first real test: sending a photo closed Oiê and logged the tablet out of WhatsApp, and nothing on the tablet said why._

- **Actor and job story:** the parent, in the parent area. *When Oiê closes by itself or stops working, I want to hand over what happened in one tap, so it can be fixed without me describing it.*
- **Success measure:**
  - after a crash, the parent can copy a report in 2 taps from Ajustes e ajuda (open the section, then Copiar);
  - the report holds what's needed to find the cause and nothing private.
- **Entry:** Parent area › Ajustes e ajuda › **Problemas**, behind the PIN. There's no entry on the kid's side: the kid isn't asked to report anything.
- **Product law:** the report stays on the tablet. Oiê never sends it anywhere. The parent copies it and chooses where to paste it.

## What the report holds
- The app version, the Android version and the tablet model.
- Up to 300 recent lines, oldest dropped first, of:
  - how the link changed (connecting, connected, disconnected, logged out with WhatsApp's reason code, blocked);
  - each send's kind and result, OK or the error;
  - the WhatsApp library's warnings and errors;
  - a crash with its stack trace;
  - **since v0.15**, what ended Oiê when it wasn't an ordinary crash, added the next time Oiê opens:
    - a fatal error in the WhatsApp library (Go), which would otherwise vanish with the app;
    - Android's own record of why the app ended (low memory, a native crash, killed, not responding), each exit once.
- **Why v0.15:** the owner saw Oiê drop back to the home screen with no crash message, and Oiê vanish from WhatsApp's Dispositivos conectados. That kind of exit never reaches the app's own crash handler.
- **Never:**
  - message text, names, photos;
  - phone numbers: any run of 6 or more digits is masked to its last 2 ("••••••12").

## Steps
| # | Screen | The parent | The system |
|---|---|---|---|
| 1 | Ajustes e ajuda | Scrolls to **Problemas** | If Oiê closed by itself, the first line says when: "O Oiê fechou sozinho em 19/11 às 16h20." Otherwise: "Nenhum problema guardado." |
| 2 | Same | Taps **Copiar relatório** | Copies the report to the clipboard; the button says "Copiado" for a moment (Android 13+ also shows its own copy confirmation). |
| 3 | Elsewhere | Pastes it where they choose | Nothing is sent by Oiê. |
| 4 | Same | Taps **Apagar relatório** (only when there is one) | Clears it at once; nothing personal is lost, so there's no confirmation. |

## States
- **Empty:** "Nenhum problema guardado." There's nothing to copy or clear.
- **With lines but no crash:** Copiar and Apagar are shown, with no crash line.
- **After a crash:** the crash line comes first, then Copiar and Apagar.

## Re-linking after WhatsApp logs the tablet out (fixed in v0.14)
- Before, after a logout, "Conectar o WhatsApp" reused the stopped connection and showed "Este tablet não consegue usar a conexão".
- Now a logout drops that connection, so Conectar o WhatsApp starts fresh with a new QR. A temporary ban still stops everything and is never retried (rule L9).

## Flow review (ux-user-flows §13)
| # | Question | Result |
|---|---|---|
| 1 | Job | ✅ One actor, one job. |
| 3 | Happy path | ✅ Two taps. |
| 5 | States | ✅ Empty, lines, crash. |
| 7 | Control | ✅ The parent decides where it goes; clearing it is one tap. |
| 13 | Accessibility | ✅ Buttons with words; 48dp targets. |
| 14 | Ethics | ✅ Nothing leaves the tablet by itself; nothing personal in it; said under the buttons. |
| 15 | Evidence | 🟡 Does it catch the photo crash? The owner's next test will say. |
