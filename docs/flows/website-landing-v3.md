# Flow: The landing page v3 (the infomercial)

_Designed with `ux-user-flows` on 2026-11-21 (owner's request; decision 0014)._

- **Actor and job story:** a parent of a 9–12-year-old, often on a phone, who arrived from a link or a friend. *When I hear about Oiê, I want to get the idea in seconds and know it's safe to try, so I can put it on the family tablet.*
- **Success measure:** "Baixe já" reached in one scroll-through. The idea (the home phone, back, on the tablet) is understood from the first screen.
- **Entry points:** a shared link, search, the safety page's logo.

```mermaid
flowchart TD
  hero([Lembra do telefone de casa? · Baixe já]) --> problem[O problema: celular com feed, anúncio e estranho]
  problem --> reveal[Apresentamos: Oiê, o telefone de casa no tablet]
  reveal --> easy[É fácil assim: 1, 2, 3]
  easy --> more[Mas espera, tem mais: só quem você escolhe, sem anúncio, sem servidor, horário de silêncio]
  more --> versus[Celular x Oiê]
  versus --> price[Quanto custa? R$ 0,00]
  price --> cta[Baixe já + como instalar]
  cta --> aviso[Aviso, em letra legível]
  aviso --> faq[Perguntas]
  faq --> foot([Código aberto, feito por Gabriel Lopes])
  hero -- Baixe já --> cta
  hero -- Segurança --> safety([Página de segurança])
```

## Steps
| # | Scene | What the visitor sees | Action |
|---|---|---|---|
| 1 | Abertura | "Lembra do telefone de casa?" and the tablet dressed as a phone, ringing | Baixe já (jumps to the download), Segurança |
| 2 | O problema | A smartphone bursting with feed, ads and strangers | Scroll |
| 3 | Apresentamos | Oiê: the home phone, on the tablet | Scroll |
| 4 | 1, 2, 3 | Touch the photo; talk, send audio, photo or sticker; it reaches the family's WhatsApp | Scroll |
| 5 | Tem mais | Four stickers: only who you choose, no ads, no server of ours, quiet hours | Scroll |
| 6 | Celular x Oiê | A plain comparison, with words and marks, never colour alone | Scroll |
| 7 | Preço | R$ 0,00, open source | Scroll |
| 8 | Baixe já | The download, "abra no tablet Android da criança", three install steps | Download |
| 9 | Aviso | The WhatsApp notice at body size | Read |
| 10 | Perguntas | The FAQ (open and close) | Read |
| 11 | Créditos | Open source, made by Gabriel Lopes, repo and website links, the safety page | Links |

## Branches
| At | If | Then |
|---|---|---|
| Any | "Remove animations" is on | Nothing rings or slides; every scene shows its end state. |
| 8 | The visitor isn't on an Android tablet | The note says to open the page on the kid's tablet; the install steps cover a blocked install. |
| Any | English | The EN switch swaps every line. |

## Flow review (ux-user-flows §13)
- **Happy path:** shorter than v2. There's no picker to fill in, and the download is reachable from the first screen.
- **Ethics:** no fake urgency ("só hoje"), no fake prices or testimonials, and the risk is readable.
- **Evidence:** the friend's reaction is the test. Show v3 to the same friend.
