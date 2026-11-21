# Flow: Groups as groups (v0.46)

_Designed with `ux-user-flows` on 2026-11-21, at the owner's request:_
- _"if there are other persons in the group that are not on the allowance of people from the kid, they also should be there, because a group is a group … If a parent allows a kid to be in a group … we want to allow the kid to read that … people that are not on their list should have a visual flag."_
- _"On the groups, the messages also don't bring who sent them … we must have the names of people. So treat the group as a group."_
- _"allow the kids to react to messages received or any type of media … tap, hold and select some emojis."_

- **Actor and job story:** the kid in a group the parent allowed (the cousins, the family, the school friends). *When the group is talking, I want to see everything that's said and who said it, so I can follow along and join in like everyone else.*
- **Success measures:**
  - every message in an allowed group shows on the tablet with its sender's name;
  - the kid can tell at a glance whether a sender is one of their contacts;
  - the kid can react to any message, media included, with a long press.

## How it works in messaging apps (the conventions followed)
WhatsApp, Telegram and Signal agree on these:
- **Sender name:** others' messages carry the sender's name above the bubble, on the first message of a run. The kid's own messages carry no name.
- **Runs:** consecutive messages from one sender show the name once.
- **Unknown senders:** a name that isn't in your contacts is shown as WhatsApp gives it (their profile name, or the number). WhatsApp marks it with "~".
- **Reactions:** a long press on any message, media included, opens the emoji row. The reaction shows under the bubble.

Oiê keeps these conventions. It uses a word instead of a symbol for the unknown sender ("Fora dos contatos"), because a kid of 9–12 won't read "~".

## Who reaches the kid in a group (decision 0016)
| Group | Before (v0.45) | Now |
|---|---|---|
| An allowed group | Only messages that start with the kid's name, replies to the kid, or the 24 h after the kid wrote. "Conversa só de Tito" opened everything. | **Everything** in the group, from everyone in it. "Conversa só de Tito" now matters for people only. |
| A group the parent didn't allow | Nothing | Nothing (unchanged) |
| The parent's own messages in an allowed group | Never shown | **Shown**, under the parent's name, because they're part of the group's conversation |
| A person (1:1), not a group | Name, reply or 24 h | Unchanged |

**The product law, reworded:** the kid can never reach, or be reached by, a contact the parent didn't allow. The one exception is inside a group the parent allowed: there the kid sees the whole group, and anyone not on the kid's list is marked. The parent allows a group knowing who is in it. The contact editor says so before the switch.

## Steps
| # | Screen | What the kid does | What the system does |
|---|---|---|---|
| 1 | **Group chat** | Reads | Each run of messages from someone else starts with their name: the name the kid knows ("Vovó") for someone on the list, or their WhatsApp name (or number) with a "Fora dos contatos" tag for anyone else. The parent's own messages carry the parent's name. |
| 2 | **Home** | Sees the group's card | The preview starts with who said it: "Vovó: …". |
| 3 | **Notification** | Sees it | "Primos" with "Vovó: Domingo tem bolo". |
| 4 | **Long press on any message** (text, photo, video, GIF, voice, sticker) | Holds | The Reagir sheet opens (as for text since v0.33). A tap still opens the photo or plays the voice. |
| 5 | **Picks an emoji** | Taps | The reaction goes to the group, as from the parent's account, and shows under the bubble. |

## Branches
| At | If | Then |
|---|---|---|
| 1 | The sender's name is unknown (no profile name) | Their number, formatted, with the tag. |
| 1 | A sender gets added to the kid's list later | The tag goes and the kid-facing name shows, on old messages too (worked out when shown, not stored). |
| 1 | The kid's own message comes back from WhatsApp | Recognised (the tablet's ids or the kid's signature) and never shown twice. |
| 4 | TalkBack | The long-press action "Reagir" on every bubble, media included. |
| — | The group is paused by the parent | Nothing reaches the kid (unchanged). |

## States
Unchanged: media still loads, fails and retries; quiet hours hold notifications.

## Flow review (ux-user-flows §13)
| # | Question | Answer |
|---|---|---|
| 1 | Job | ✅ One actor, one job, three measures. |
| 3 | Happy path | ✅ No new taps. Reacting is one long press and one tap, on anything. |
| 4 | Branches | ✅ Unknown name, added later, own echo, TalkBack, paused. |
| 9 | Memory | ✅ The name is on the message, so there's no guessing who said it. |
| 11 | Conventions | ✅ As in WhatsApp, Telegram and Signal. |
| 13 | Accessibility | ✅ The name and "fora dos contatos" are read with the message. The flag is a word, not a colour. |
| 14 | Ethics | 🟡 More reaches the kid in groups. This is the owner's decision (0016), told to the parent in the contact editor and in Ajuda. |
| 15 | Evidence | 🟡 Try it in the cousins' group: names, one unknown member, a reaction on a photo. |
