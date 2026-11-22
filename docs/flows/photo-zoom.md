# Flow: Zooming into a photo (v0.53)

_Designed with `ux-user-flows` on 2026-11-22, at the owner's request: "Right now the kid cannot pinch to zoom the images, so they should be able to do it."_

- **Actor and job story:** Tito, looking at a photo the family sent. *When grandma sends a photo of the cake, I want to see the details (who's in it, what's written on it), so I can enjoy it and answer about it.*
- **Success measure:** any part of a photo can be seen at up to 4×, and back to the whole photo in one gesture.
- **Entry:** a tap on a photo in a chat opens it on its own screen (since v0.33).

## Steps
| # | What Tito does | What happens |
|---|---|---|
| 1 | Pinches out with two fingers | The photo grows around his fingers, up to 4×. |
| 2 | Drags with one or two fingers | The zoomed photo moves, never further than its own edges. |
| 3 | Pinches in | It shrinks, never below the whole photo (1×). |
| 4 | Double-taps | At 1× it zooms to 2.5× around that point; zoomed in, it goes back to the whole photo. |
| 5 | Back | Closes, as before. The next photo opens whole. |

## Branches
| At | If | Then |
|---|---|---|
| 1, 4 | "Remove animations" is on | The double-tap jumps straight to the new size; pinching follows the fingers as always. |
| Any | TalkBack | The photo has two actions, "Ampliar" and "Ver inteira" (zoom in, back to whole), and says "Ampliada" while zoomed. One pointer is enough everywhere (WCAG 2.5.1: double-tap or the actions). |
| 2 | At 1× | A drag does nothing (the whole photo is in view). |

## Not here
- **Photos inside the chat and the statuses:** they stay as they are. A tap already opens a chat photo big; a status moves by itself.
- **Zoom buttons on screen:** not added (less-is-more). Double-tap and the TalkBack actions already cover anyone who can't pinch.

## Flow review (ux-user-flows §13)
| # | Question | Answer |
|---|---|---|
| 1 | Job | ✅ See the details of a family photo. |
| 3 | Happy path | ✅ The pinch every tablet already uses. |
| 6 | Errors | ✅ No way to lose the photo: the zoom is clamped and Back always closes. |
| 7 | Control | ✅ Double-tap or pinch back to the whole photo. |
| 11 | Conventions | ✅ The same as the Photos app and WhatsApp. |
| 13 | Accessibility | ✅ Single-pointer ways (double-tap, TalkBack actions), and the state is said. |
| 15 | Evidence | 🟡 Tito zooms into a family photo on the tablet. |
