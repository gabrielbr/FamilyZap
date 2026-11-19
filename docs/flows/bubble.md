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

## v0.29: a bubble for each active chat
_The owner, 2026-11-19: "bubbles for each of the chats that are active. If the kid is talking with two people, there are two bubbles. Tapping one opens just that chat, with the text field focused, so the keyboard comes up."_

- **Which chats:** while "Bolha na tela" is on, a chat gets its own bubble when a message reaches the kid or the kid sends one. That's Android's own conversation bubble, the way messaging apps do it. Each shows the person's picture (or their letter), and two active chats mean two bubbles. The general Oiê bubble stays, for reaching everyone else.
- **Tapping a chat's bubble:**
  - opens only that chat, in the small window: its messages, the camera, stickers and the microphone;
  - the text field is already focused and the keyboard is up, to answer straight away;
  - Back, or the arrow, folds the window back into the bubble;
  - there's no way from it to the other chats or to the parent area (that's the Oiê bubble's job).
- **A new message** in a chat with a bubble shows on the bubble (Android's dot) and in the notification shade as before.
- **The kid's own send** keeps that chat's bubble on screen, silently (no notification for one's own message).
- **A paused or removed chat** loses its bubble the next time Oiê opens, and the window closes if it's open.
- **Quiet hours:** the chat's window says so in place of the field, as in the full app; the keyboard doesn't come up.
- **Limits:** Android shows a handful of bubbles at once and may group them; that's the system's choice.

| # | Who | Does | The system |
|---|---|---|---|
| 1 | Vovó | Writes "Tito, oi" | A notification and Vovó's bubble (her picture). |
| 2 | The kid | Taps Vovó's bubble | Vovó's chat in the small window, the keyboard up. |
| 3 | The kid | Types, sends | Sent as from the full app; 5-second undo. |
| 4 | The kid | Back | The window folds; the bubble stays. |

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
