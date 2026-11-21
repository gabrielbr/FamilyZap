# Audit · website · no "meeting outside"

- **Date:** 2026-11-21
- **What changed:** words only, in `site/index.html` and `site/assets/site.js` (PT and EN):
  - the hero line;
  - story step 4;
  - the "Sem anúncio, sem feed" highlight;
  - the school friend's example message and reply.

  Decision 0014, update.
- **Why:** the owner: "it also seems shady that they will meet people outside. So I want to avoid this way to talk about it."
- **Skills used and how:** ux-user-flows (§13), graphic-design-fundamentals (§13) and less-is-more were loaded and read earlier in this session. impeccable's audit dimensions are applied to a words-only change; the layout is unchanged.
- **Checked by machine:**
  - axe at 320–1440, PT and EN, light and dark: 0 violations;
  - no sideways scroll, also at 200% text;
  - reduced motion OK; no script errors.
- **Looked at:** the hero, at desktop size.

## 1. ux-user-flows (§13)
No flow change. **Ethics ✅:** the page no longer suggests that talking leads to meeting people. **Evidence 🟡:** show it to the friend who gave the first feedback.

## 2. impeccable
20/20. Same layout, and the hero line is a similar length (it wraps to three lines, as before). No findings.

## 3. graphic-design-fundamentals (§13)
**Brief ✅, concept ✅:** one idea, the home phone for keeping in touch. **Consistency ✅:** PT and EN say the same thing.

## 4. less-is-more
| Element | Question (1–5) | Kept / moved / merged / removed | Where it went |
|---|---|---|---|
| "o encontro acontece lá fora" (hero, highlight) | 4 (weak, and it reads wrong) | Replaced | "contar do dia e matar a saudade", "conversar com quem a criança gosta" |
| "o resto acontece fora da tela" (step 4) | 4 | Replaced | "Simples como era o telefone de casa." |
| Friend's "Te espero na quadra" | 4 | Replaced | "Viu o jogo ontem?" / "Vi sim, que golaço." |

Taps unchanged; 4/4 on every dimension.

## 5. Copy
No "!", no em dashes, no hype.

## Open items
- **The safety page** (`seguranca/`, `safety/`) still opens with the 90s story ("Combinava a bicicleta… o encontro acontecia lá fora") and closes with "conversar para se encontrar. O resto acontece fora da tela." The owner asked about the landing page only; the safety page is flagged to them.

## Impeccable polish (same day, the owner: "Use impeccable polish on the landing page")
Polish (`reference/polish.md`) with impeccable's context loaded (PRODUCT.md, DESIGN.md, the surface brief).
- **What I looked at:**
  - every scene, on desktop (1280) and phone (375), in PT and EN;
  - "Próximo" and slow scrolling through the story on a phone;
  - the detector, run once.
- **Fixed:**
  - **P2, a pronoun with nothing before it:** step 2 opened "Ela sai pela linha da casa…", but no heading before it names a message. It's now "A mensagem sai pela linha da casa: o seu WhatsApp." (EN: "The message goes out on the family line: your WhatsApp.")
  - **P2, the fragment cadence (detector "aphoristic cadence", added by today's words):** "telefone de casa" appeared about eight times, and four lines were tacked-on fragments. The fixes:
    - the title is now "Oiê: o telefone de casa, agora no tablet";
    - the hero is one sentence ("Ele voltou, agora no tablet: …");
    - "Como o telefone de casa, que era de todo mundo." was cut from step 2;
    - "Simples como era o telefone de casa." was cut from step 4.

    The image stays where it carries meaning: the hero question, step 1, the phone book in step 6, and the highlights heading.
- **Left, and why:**
  - **The aphoristic-cadence match on "No seu celular, …":** a false positive, since in Portuguese "No" means "on the".
  - **"Sem análise de uso, sem anúncios, sem rastreamento":** the plain privacy facts the product rules ask for.
  - **Cramped padding, nested cards and the pulsing dot:** the drawn devices and WhatsApp's typing dots, already dispositioned by the independent review on 2026-11-20.
- **Checks after the fixes:** axe clean at 320–1440 in PT and EN, light and dark; no sideways scroll at 200% text; reduced motion OK; no script errors.
- **Scores:** critique 40/40 and audit 20/20 hold. No P0 or P1.
