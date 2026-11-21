# Audit · website · the family page v2, "Por que um tablet?", and groups

- **Date:** 2026-11-21
- **The owner:**
  - "something that parents would share with contacts or groups … very user-friendly and very educative and avoid a lot of wording, just clear messages … how do people in a group contact the kid and not the parent";
  - "add a FAQ item that says why a tablet and not a cell phone … a slate that kids cannot carry around all the time … avoid getting kids addicted to cell phones, but allow them to communicate."
- **What changed:**
  - `falar-com-a-crianca/` and `talk-to-a-child/` rewritten as five steps with sample chats;
  - `read.css` (the chat samples and the "Bom saber" block; the old two-column lists removed);
  - the landing page: FAQ q12 (also in the JSON-LD), the highlight f1 and a2 about groups;
  - the safety pages' "Desconhecidos" line;
  - `MessageRuleTest.theWebsitesExamplesHold` updated to the new examples.
- **Skills used and how:** all four were loaded and read in this session. `ux-user-flows` updated the flow (`docs/flows/website-talk-to-a-kid.md`, v2). `impeccable` and the others were run on the 375 and 1280 px screenshots, light and dark.

## 1. ux-user-flows (§13)
- **Job ✅:** a grandparent or a friend's parent, from a link in a group, learns in one scroll how to reach the child, and how to reach the parent instead.
- **Memory ✅:** each rule sits next to its example.
- **Accessibility ✅:** headings run in order. Each sample message carries a hidden "Você:" or "Tito:". The tags are words.
- **Evidence 🟡:** share it in one family group and see if someone writes "Tito, …" first time.

## 2. impeccable (audit, Read mode)
| Dimension | Score 0–4 | Note |
|---|---|---|
| Accessibility | 4 | axe (WCAG 2.2 AA plus best practice): 0 violations on all six pages, 320 to 1280 px, light and dark. 200% text has no overflow. |
| Performance | 4 | Static; no script on the family page. |
| Theming | 4 | Tokens only. The chat ground is `line`, the bubbles `surface`, the tags ink or muted. |
| Responsive | 4 | Bubbles cap at 92% of the sample, which caps at 32rem. |
| Anti-patterns | 4 | Not a WhatsApp screen (no WhatsApp colours or logo). The step numbers carry the order. |
| **Total** | 20/20 | |

| ID | Severity | Finding | Fix | Status |
|---|---|---|---|---|
| W1 | P1 | The page was a wall of text for a shared link (the owner). | Five steps with sample chats; 60% fewer words. | ✅ fixed |
| W2 | P2 | Step 4's sample didn't say it was a group. | A "Grupo Primos" title in that sample. | ✅ fixed |
| W3 | P1 | The site said "no one else reaches your kid", which is no longer true in groups (v0.46). | f1, a2 and the safety pages now say what groups show. | ✅ fixed |

## 3. graphic-design-fundamentals (§13)
- **Hierarchy ✅:** the step headline, then one line, then the sample. The tags are smaller than the message.
- **Contrast ✅:** "Chega na criança" has an ink outline; "Fica com os pais" is muted. They differ in words and in weight.
- **Consistency ✅:** the same rings and reading column as the safety page.

## 4. less-is-more
| Element | Question (1–5) | Kept / moved / merged / removed | Where it went |
|---|---|---|---|
| Six "Bom saber" items | 3 (understand once) | Three lines | — |
| The parent taking over mid-chat | 3 (rare, and hard to say briefly) | Removed from the page | The app's Ajuda |
| Kid-only chats | 5 (rare, the parent's setting) | Removed from the page | The app |

## 5. The FAQ "Por que um tablet e não um celular?"
- It's in the owner's words: a tablet stays home and doesn't go in a pocket, so the kid talks at calmer moments. It's about talking without a phone too early.
- It cites the federal guide (the same Secom source as the safety page).
- It's honest: Oiê works on a phone if the family chooses one.

## Checks
- `v3check.js` (landing, PT and EN: axe, overflow, 200%, reduced motion, no script errors) and `talk.js`/`nav.js` on the other pages: clean.
- The JSON-LD parses.
- `MessageRuleTest` passes.
