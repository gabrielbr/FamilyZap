# Flow: One small animation for every action (v0.49)

_Designed with `ux-user-flows` on 2026-11-21, at the owner's request: "Make that every action of the user has one small interaction animation."_

- **Actor and job story:** anyone using Oiê, mostly the kid. *When I touch something, I want to see right away that it heard me, so I know my tap worked and don't tap twice.*
- **Success measure:** every touch target responds while it's pressed (Nielsen's 0.1 s, "feels instant"), and every change of state is visible as a change, not a jump.

## The system's motion rules (AGENTS.md, unchanged)
- 150 ms standard, 250 ms emphasised, one ease (`FastOutSlowInEasing`), no bounce.
- **Press scales to 97%.**
- **"Remove animations" (Android):** things change at once, with nothing moving. The colour still changes, so the feedback stays.

## One animation per kind of action
| Action | The animation | Where |
|---|---|---|
| **Tap** (buttons, rows, cards, tiles, links, chips, keys, emojis, stickers, tabs) | Scales to 97% while held and back on release, 150 ms. | Everywhere, through `pressable` and the new `pressableSelect`, `pressableToggle`, `pressableCombined`. |
| **Long press** (a message, to react) | The same 97% while held. The sheet opens when the press is long enough. | Message bubbles (`pressScaleOnHold`). |
| **Pick one of several** (tabs, Lista/Fotos/Mosaico, days, colours, contacts in setup) | The 97% press, then the fill and outline fade to "picked" in 150 ms. | `motionColor`. |
| **Open or close** (a help answer) | The row grows or shrinks to fit, 150 ms. | Ajuda › Perguntas (`animateContentSize`). |
| **Send or receive** (a new message) | The message fades in and the list makes room smoothly, 150 ms. | The chat (`animateItem`). |
| **Switch** (on or off) | The thumb slides (Android's own switch), plus the row's 97% press. | Every `SwitchRow`. |

Things that are already their own feedback get no extra animation:
- drawing (the line is the feedback);
- recording (the clock runs);
- a dragged row in Arrumar (it moves with the finger).

## Flow review (ux-user-flows §13)
| # | Question | Answer |
|---|---|---|
| 1 | Job | ✅ Every tap is seen as heard. |
| 11 | Conventions | ✅ A press state and gentle transitions, as on Android. |
| 12 | Speed | ✅ Within 0.1 s; nothing waits for an animation (taps act on release, as before). |
| 13 | Accessibility | ✅ "Remove animations" respected. No flashing, nothing large moves. |
| 14 | Ethics | ✅ No attention-grabbing motion: only responses to the person's own touch. |
