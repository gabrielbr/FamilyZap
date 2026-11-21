# Audit · website · the example on phones, and "O Oiê é seguro?"

- **Date:** 2026-11-21
- **What the owner asked:**
  - "Some users are mentioning that on mobile version the example area is a little bit confusing. It doesn't look like they need to scroll down."
  - A FAQ question on whether Oiê is secure, saying it's open source so people can check the code.
- **What changed:**
  - `site.css` (phone stage 54svh, the tag at the stage's bottom edge, the step bar, step spacing);
  - `site.js` (the bar; the reading line a third of the way into the band);
  - `index.html` (the bar, the FAQ entry and its JSON-LD).
- **Skills used and how:** this session loaded and read ux-user-flows, impeccable (craft floor), graphic-design-fundamentals and less-is-more. Each is applied below, on screenshots at 390×844, 360×640 and 768×1024.

## Why it confused people (diagnosis)
1. On a phone, the pinned stage filled 62% of the screen and looked like a whole, finished screen. Below it, the 38% band showed one step at a time, and the next step's title didn't show until the reader had already scrolled.
2. The only signs of a sequence were "Exemplo · 2 de 6" and "Próximo", in small type at the stage's top right. That's the farthest point from both the thumb and the words.
3. The heading of the step being read often sat half under the stage's edge, which looked like a glitch rather than "keep going".

## Ideas weighed
| Idea | Verdict |
|---|---|
| A bouncing "scroll" arrow | No. It's motion as decoration, and it says nothing about how long the story is. |
| A swipe carousel on phones | No. It hides the content behind a gesture, changes the page's structure, and is worse for screen readers. |
| A progress bar of six steps | **Yes.** It shows the length and where you are at a glance. |
| Move the count and "Próximo" to the stage's bottom edge | **Yes.** It's next to the words and in reach of the thumb. "Próximo" stays the one way on, in one place. |
| A shorter stage, with steps shorter than the band | **Yes.** The next title always peeks in, and a cut-off next item is the strongest "there's more" cue. |
| A fade at the stage's edge | No. The design system has no gradients. The hairline stays. |

## 1. ux-user-flows (§13)
- **Progress ✅:** the bar and "N de 6" show the length and the position.
- **Conventions ✅:** a stepper and "Próximo" are familiar. Scrolling still works the same way.
- **Accessibility ✅:**
  - the bar is `aria-hidden`; the words "Exemplo · 2 de 6" carry the meaning;
  - "Próximo" keeps a 44px target;
  - reduced motion: the bar changes without a transition.
- **Evidence 🟡:** ask two of the people who were confused to try it on their phones.

## 2. impeccable (audit)
| Dimension | Score 0–4 | Note |
|---|---|---|
| Accessibility | 4 | axe (WCAG 2.2 AA plus best practice): 0 violations, PT and EN, 320 to 1440 px, light and dark. 200% text has no overflow. |
| Performance | 4 | Six spans and one class toggle per step. |
| Theming | 4 | Ink and muted tokens; the remaining segments use muted at 35%. |
| Responsive | 4 | On "Próximo", every step lands with its heading 16px clear of the stage at 390×844, 360×640 and 768×1024 (measured). |
| Anti-patterns | 4 | No decoration. The bar carries information (where you are in the story). |
| **Total** | 20/20 | |

| ID | Severity | Finding | Fix | Status |
|---|---|---|---|---|
| M1 | P1 | The phone story didn't read as scrollable. | The bar, a lower tag, a shorter stage, the next title peeking. | ✅ fixed |
| M2 | P2 | The current step's heading sat under the stage's edge. | The reading line is now a third of the way into the band, and each step has 32px of top padding. | ✅ fixed |

## 3. graphic-design-fundamentals (§13)
- **Hierarchy ✅:** picture, then the progress line, then the words. The bar is quieter than the heading and louder than the hairline.
- **Proximity ✅:** "Próximo" sits next to what it moves.
- **Consistency ✅:** the same count and link as before, moved.

## 4. less-is-more
| Element | Question (1–5) | Kept / moved / merged / removed | Where it went |
|---|---|---|---|
| The count and "Próximo" at the top | 1 (helps now, but far from the action) | Moved | The stage's bottom edge, on phones |
| A second "continue" hint | 2 (duplicate) | Not added | The bar and the peeking title do the job |

Taps on the common action: unchanged (scroll, or one tap on "Próximo").

## 5. The FAQ entry "O Oiê é seguro?"
- **Copy:**
  - It's the first question, because it's the one parents ask first.
  - It says what keeps the risk low: no server, only approved people, the PIN, and conversations encrypted on the tablet (checked: `Persistence.kt`, AES-256-GCM with a Keystore key).
  - It says what it can't promise: no "100% safe". The WhatsApp restriction risk is said plainly (product law).
  - It says the code is open, with a link to read it.
- **Accessibility:** the link's text says where it goes ("ler o código no GitHub").
- **Structured data:** the FAQPage JSON-LD includes it, and it parses.

## Checks
- `node v3check.js`: axe, overflow, 200% text and reduced motion are clean, with no script errors.
- The "Próximo" landing measurement and phone screenshots were looked at.
- `python3 design/build.py --check`.
