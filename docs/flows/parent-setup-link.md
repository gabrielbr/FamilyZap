# Flow: Parent setup + link

_Stage 5. Designed with `ux-user-flows` on 2026-11-18. The owner decided on 2026-11-18:_
- _the parent area lives on the kid's tablet behind a 6-digit Oiê PIN, with no parent app and no server;_
- _setup asks for the kid's name, the parent's name, the first contacts and quiet hours._

- **Actor and job story:** a parent with the family's Android tablet and their own phone with WhatsApp. *When I want my kid to talk to the family without my phone, I want to set up Oiê on the tablet once, knowing the risk, so I can hand the tablet over and know they only reach the people I chose.*
- **Success measure:** setup finished in **under 5 minutes** by a parent on their own, with no help. Drop-off is measured at each step. The risk notice is never skipped.
- **Entry points:**
  1. First open of Oiê on the tablet, after installing it.
  2. Returning mid-setup: Oiê reopens at the step it stopped on.
  3. **Reconnect:** the link dropped, so the kid's side says "Peça para {nome} conectar de novo", and the parent opens the parent area to scan again.
  4. **Forgot PIN:** relinking WhatsApp proves it's the parent.
- **Preconditions:**
  - an Android tablet with internet;
  - the parent's phone with WhatsApp, nearby;
  - WhatsApp allows another linked device (WhatsApp limits how many a number can have).
- **Taps on the common path:** about 12 taps plus typing two names and a PIN. Linking is the slow part, about 30–60 s on the phone.

```mermaid
flowchart TD
  start([First open on the tablet]) --> welcome[Welcome, for the parent<br/>'Oiê é para seu filho ou filha conversar<br/>com a família pelo seu WhatsApp'<br/>Começar]
  welcome --> risk[Risk notice<br/>Entendi, conectar / Agora não]
  risk -- Agora não --> later([Closes. Next open: the welcome again])
  risk -- Entendi, conectar --> link[QR on the tablet + 4 steps for the phone]
  link --> linked{Scanned and linked?}
  linked -- QR expired --> link
  linked -- WhatsApp refused, too many devices --> tooMany[Remove a device in WhatsApp, then try again] --> link
  linked -- Yes --> names[Your name, prefilled from WhatsApp<br/>Your kid's name]
  names --> pin[Create a 6-digit PIN, twice]
  pin --> contacts[Choose who your kid can talk to<br/>at least 1, or Depois]
  contacts --> quiet[Quiet hours<br/>Pular or set times + exceptions]
  quiet --> done[Pronto. Entregue o tablet para {criança}]
  done --> kid([Kid side: the kid notice, then the home])
```

## Steps
"Passo x de 6" shows on steps 1–6.

| # | Screen | What the parent does | What the system does |
|---|---|---|---|
| 0 | **Welcome** | Reads one line, taps **Começar** | "Oiê é para seu filho ou filha conversar com a família pelo seu WhatsApp, só com quem você escolher." Shows the wordmark and one cluster of rounds. |
| 1 | **Risk notice** | Reads and taps **Entendi, conectar** or **Agora não** | Shows the approved copy (`01 Brand/voice.md`) in full, not folded and not in small print. **Entendi, conectar** is the one coral button. **Agora não** is a quiet button with the same reach. |
| 2 | **Link** | On the phone: WhatsApp › **Configurações** › **Dispositivos conectados** › **Conectar um dispositivo**, then scans the QR on the tablet | Shows a big QR code and the 4 steps, numbered, in the parent's words. The QR refreshes on its own before it expires. After the scan, "Conectando…" with progress. On success: "Conectado ao WhatsApp de {perfil}". |
| 3 | **Names** | Checks their own name (prefilled from the WhatsApp profile name) and types the kid's name | The kid's name tags messages ("Tito: …"). The parent's name is used in the kid notice. Both can be changed later in the parent area. |
| 4 | **PIN** | Types 6 digits, then again | "Esse PIN abre a área dos pais neste tablet. Não use o mesmo PIN do tablet." Rejects 000000, 123456 and the like, with a plain reason. |
| 5 | **First contacts** | Searches and picks family from their WhatsApp contacts and groups, then taps **Continuar** | Shows the parent's contacts with search. Picked ones go to the top with a check and `selected`. At least one keeps the kid's home from being empty; **Depois** is allowed (the kid's home then shows its empty state). Details in flow "Choose contacts + quiet hours". |
| 6 | **Quiet hours** | Taps **Pular**, or sets times (e.g. school 7h–12h, bed 21h–7h) and optional exceptions (contacts allowed even then) | Default: none. Exceptions per contact (the owner's decision, 2026-11-18). |
| 7 | **Done** | Hands the tablet over | "Pronto. Entregue o tablet para {criança}." One button: **Abrir para {criança}**. The kid side opens with the kid notice. |

## Branches
| At step | If | Then |
|---|---|---|
| 1 | **Agora não** | Oiê closes. Nothing is linked or stored. The next open starts at the welcome again. No nagging. |
| 2 | The QR expires | It refreshes on its own; nothing to tap. |
| 2 | The phone isn't near, or the parent leaves | The step is kept. Coming back reopens the QR. |
| 2 | WhatsApp refuses because of too many linked devices | "O WhatsApp já tem o máximo de aparelhos conectados. No celular, remova um em Dispositivos conectados e tente de novo." **Tentar de novo**. |
| 2 | WhatsApp refuses for another reason (e.g. the number is restricted) | "O WhatsApp não aceitou a conexão agora. Tente mais tarde." No tech words; the detail is kept for Ajuda. |
| 2 | Linking takes over 10 s | Progress with "Isso pode levar até 1 minuto" and **Cancelar**. |
| 2 | Tablet offline | "Sem internet. Conecte o tablet ao Wi-Fi para continuar." The QR is hidden until it's back. |
| 3 | Kid's name empty | **Continuar** stays off. The hint "Como a família chama seu filho ou filha?" is the label, above the field. |
| 4 | PINs don't match | "Os PINs não são iguais. Digite de novo." Only the second field clears. |
| 4 | An easy PIN | "Esse PIN é fácil de adivinhar. Escolha outro." |
| any | Back | Returns one step; everything typed is kept. From the link step on, back never unlinks. |
| any | The parent closes Oiê mid-setup | Reopens at the same step. The kid side isn't reachable until setup is done. |

## Reconnect and forgot PIN
- **Reconnect** (the link dropped). The kid's side says "Peça para {nome} conectar de novo." The parent opens **Área dos pais** with the PIN, sees "O Oiê se desconectou do seu WhatsApp", and taps **Conectar de novo**, which goes back to step 2. Messages the kid sent meanwhile are kept and go out after.
- **Forgot PIN.** On the PIN screen: **Esqueci o PIN**, then relink WhatsApp (step 2) with the same number, then set a new PIN. Being able to link the same WhatsApp proves it's the parent.
- **Unlink** (parent area): **Desconectar do WhatsApp** is destructive and can't be undone from the tablet. It is the one place with a confirm: "O {criança} não vai conseguir mandar nem receber mensagens até você conectar de novo." **Desconectar** (stop fill) / **Cancelar**.

## States per step
| Step | Empty | Loading | Error | Success | Offline |
|---|---|---|---|---|---|
| Link | — | "Conectando…" with a cancellable wait | Refused / too many devices (above) | "Conectado ao WhatsApp de {perfil}" | QR hidden, Wi-Fi line |
| Names | Kid field empty → Continuar off | — | — | — | Works offline |
| Contacts | No contacts loaded yet → skeleton list | Loading contacts | "Não deu para carregar seus contatos. Tentar de novo" | Count shown: "3 escolhidos" | "Sem internet…" |
| Done | — | — | — | Kid side opens | Kid side opens; sends queue |

## Exits
- **Finished:** the kid side, with the kid notice first.
- **Agora não:** Oiê closes with nothing stored. Next open shows the welcome.
- **Mid-setup:** resumes at the same step.
- **Reconnect / forgot PIN:** back in the parent area.

## Remembers
- The step reached, and everything typed in setup.
- The parent's name, from the WhatsApp profile.
- The last quiet hours used (for the parent area later).

## Notes
- **Data:** the WhatsApp link, names, PIN (hashed) and settings live on the tablet only. Nothing goes to our servers.
- **Self-update** (the core rule) applies to the tablet app from the first release. The "Check for updates" row lives in the parent area's About.
- **Distribution:** Play may not accept the app, so it may ship as a direct APK (open item in STATUS). Setup starts after install either way.
- **Copy** follows `01 Brand/voice.md`. "Dispositivos conectados" and "Conectar um dispositivo" are WhatsApp's own menu names, used only to guide the parent there.

## Flow review (ux-user-flows §13)
| # | Question | Result |
|---|---|---|
| 1 | Job | ✅ One actor (the parent), one job story, success in time and drop-off. |
| 2 | Entries | ✅ First open, resume, reconnect, forgot PIN. |
| 3 | Happy path | 🟡 Six steps. Each asks for something needed now; quiet hours and contacts allow **Pular** / **Depois**. Could quiet hours move to the parent area to shorten it? Kept, by the owner's choice. |
| 4 | Branches | ✅ Agora não, QR expiry, too many devices, refused, slow, offline, back, close, mismatch, weak PIN. |
| 5 | States | ✅ Table above. |
| 6 | Errors | ✅ Plain words, input kept, a clear next step; the PIN clears only the second field. |
| 7 | Control | ✅ Back keeps input; the only confirm is unlinking, which is destructive. |
| 8 | Progress and resume | ✅ "Passo x de 6"; resumes at the same step. |
| 9 | Memory | ✅ The parent's name is prefilled; the phone steps stay on screen during linking. |
| 10 | Exits | ✅ Ends by handing over to the kid; nothing is a dead end. |
| 11 | Conventions | ✅ Linking by QR like WhatsApp Web; PIN entry like Android's. |
| 12 | Speed | ✅ Waits over 10 s show progress and Cancelar. |
| 13 | Accessibility | 🟡 The QR is the only way to link, and it needs a camera on the phone. WhatsApp's own alternative (a code by phone number) to check in the build. |
| 14 | Ethics | ✅ The risk is said in full **before** linking, with an equal "Agora não"; no preselected options, no nagging after a no. |
| 15 | Evidence | 🟡 Test with 3–5 parents: do they understand the risk notice, and can they find "Dispositivos conectados" alone? |

**Issues**
- **Important:** a linking alternative to the QR (WhatsApp's phone-number code). Check whether the linking method offers it, and add it as "Conectar com código" if so.
- **Polish:** the 4 phone steps should follow the WhatsApp version on the parent's phone (menu names change). Keep them in one place in the code, easy to update.
