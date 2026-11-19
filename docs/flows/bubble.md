# Flow: Oiê in a bubble

_Designed with `ux-user-flows` on 2026-11-19 (v0.25), from the owner's request: "allow the app to work as a bubble. On the tablet, when the bubble is tapped, a smaller version of the app opens, taking less space and looking like a floating phone, on top of other apps."_

- **Actor and job story:** the kid, using another app on the tablet (a game, a video). *When I'm in another app and want to talk to Vovó, I want Oiê one tap away in a small window, so I don't lose what I was doing.* The parent turns it on.
- **Success measure:** from another app, the kid reaches a family member in 2 taps (the bubble, then the person), and goes back to the other app with one tap outside the window.
- **Built on Android's Bubbles** (Android 11 and later): a round Oiê bubble floats over other apps. Tapping it opens Oiê in a small window, which uses the phone-sized layout Oiê already has (the 360dp design, checked in every audit since v0.13). Android owns the bubble itself: dragging, the X to dismiss, where it sits.
- **Entry:** Parent area › Ajustes e ajuda › **Bolha na tela** (behind the PIN). It's off until the parent turns it on.

## Steps
| # | Where | Who does what | The system |
|---|---|---|---|
| 1 | Ajustes e ajuda › Bolha na tela | The parent turns the switch on | Oiê asks Android for a bubble. If Android allows it, the bubble appears at the screen's edge: "A bolha está na tela." |
| 2 | Any app | The kid taps the bubble | Oiê opens in a small window over the app: the kid's home, with the family's faces. |
| 3 | The small window | The kid taps Vovó, writes, sends | The same chat as in the full app. Messages, photos, voice and stickers work the same. |
| 4 | The small window | The kid taps outside, or Back | The window folds back into the bubble; the other app is still there. |
| 5 | The bubble | The kid drags it to the X | The bubble goes. It comes back the next time Oiê opens, while the switch is on. |

## Branches
| At | If | Then |
|---|---|---|
| 1 | Android doesn't allow bubbles for Oiê (the default on many tablets) | "O Android ainda não deixa o Oiê mostrar bolhas." and **Permitir bolhas**, which opens Android's own bubble settings for Oiê. On Samsung tablets one line more: Configurações › Notificações › Configurações avançadas › Notificações flutuantes › Bolhas. Back in Oiê, the line updates. |
| 1 | Notifications are off for Oiê | A bubble is a kind of notification, so "Ative as notificações do Oiê para ver a bolha." and the same button. |
| 1 | The tablet is older than Android 11 | "Este tablet não tem bolhas (precisa do Android 11 ou mais novo)." No switch. |
| 3 | The kid taps Pais | The PIN, as in the full app. The parent area works in the window too. |
| any | The parent turns the switch off | The bubble goes at once. |

## Notes
- **One Oiê, two windows:** the full app and the bubble show the same Oiê. Opening a chat in one opens it in the other.
- **Quiet and silent:** the bubble's own notification makes no sound and is hidden behind the bubble (Android's "suppress notification"). Messages still arrive as before.
- **Product law:** the bubble shows only what the full app shows: the allowed contacts and the parent area behind the PIN.

## Flow review (ux-user-flows §13)
| # | Question | Result |
|---|---|---|
| 1 | Job | ✅ One actor using it (the kid), one turning it on (the parent). |
| 2 | Entries | ✅ The bubble; the setting. |
| 3 | Happy path | ✅ Bubble, person: 2 taps from another app. |
| 4 | Branches | ✅ Not allowed, notifications off, old Android, dismissed, off. |
| 7 | Control | ✅ Android's own X and drag; the switch. |
| 11 | Conventions | ✅ Android's Bubbles, not a home-made overlay (no "draw over other apps" permission). |
| 13 | Accessibility | ✅ The window uses the phone layout that is checked at 1.3× text. |
| 15 | Evidence | 🟡 Android decides the window's size; the owner's tablet test will show it. |
