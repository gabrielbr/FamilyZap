# 0013 · The website tells the story with people and WhatsApp screens

- **Date:** 2026-11-20
- **Status:** approved
- **Approved by:** the owner, in the session's feedback on the first landing page ("more human … a scrollytelling page … silhouettes … use the screen of WhatsApp for the middle, the bridge, and the receiver … not the WhatsApp branding, but we can use its colors … grandma is just one of the possible receivers … kids can talk with their friends, not only the family").

## Context
The first landing page (site/, v1) drew the mechanism as an abstract route, and the owner found it dull. They asked for a page a parent understands by watching people. Its example receiver was always the grandmother, but Oiê works with anyone the parent allows, school friends included.

## Decision
**1. People:** the website tells the story with silhouettes, drawn from the logo's touching rounds:
   - a kid (son or daughter);
   - a parent (mother or father);
   - a receiver (grandma, grandpa, an aunt, a school friend).

   They are silhouettes, so there are no faces and no identifiable children (the imagery rule still holds). The visitor can pick who is in their family, and the story uses those names.

**2. WhatsApp screens:** the parent's phone (the bridge) and the receiver's phone are drawn as WhatsApp screens, in WhatsApp-like colours (green header, beige chat, green and white messages). This is the one place Oiê shows green and message bubbles.
   - They depict the family's own WhatsApp, which is honest about how Oiê works. They are labelled "seu WhatsApp" and "o WhatsApp de …".
   - There is no WhatsApp logo, wordmark or icon. The footer and the honesty section keep saying Oiê is not made or approved by WhatsApp or Meta.
   - **The kid's tablet always shows Oiê's own screen** (coral frame, message rows), so the difference between Oiê and WhatsApp stays visible.

**3. Devices:** devices appear as flat drawings held by the silhouettes. They are never glossy beauty shots.

**4. Who the kid talks to:** family and friends the parent approves. The examples rotate between grandma, grandpa, an aunt and a school friend, never only grandma.

## Options considered
| Option | Why not |
|---|---|
| Keep the abstract route (v1) | The owner found it dull and not human enough |
| Generic grey phone screens | A parent wouldn't recognise their own WhatsApp; the bridge would stay abstract |
| Real photos of families | Banned for kids' faces in marketing; silhouettes keep it human without identifying anyone |

## Consequences
- This is an exception to the brand rules on green, bubbles and devices (`02 Design System/README.md` §1 and §8, `01 Brand/imagery-and-icons.md`). It is **for the website only**; the app never shows green or bubbles.
- The website copy and FAQ say "a família e os amigos que você escolher" and name several receivers.
- The WhatsApp-like colours live only in the site's `.wa` screens, as named constants with this decision cited.

## Update, 2026-11-20: motion on the website
Scene changes in the story use the system's durations (250ms, presses 150ms) and its one ease, with no bounce. The message travelling along the wire takes 1.2s, the one longer motion; it is a story illustration, not interface feedback. Typing taps, the WhatsApp dots and the caret mimic real gestures. Nothing loops forever: the step-5 receivers go round once. With "Remove animations", everything shows its end state.
