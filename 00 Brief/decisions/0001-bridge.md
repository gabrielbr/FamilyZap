# 0001 · How FamilyZap reaches WhatsApp

- **Date:** 2026-11-18
- **Status:** approved
- **Approved by:** the owner, 2026-11-18, in the Claude session (chose option A against the recommendation, knowing the ban risk)

## Context
The brief says a kid aged 9–12 messages and calls family from an Android tablet through a
parent's WhatsApp, only with allowed contacts, with messages coming from the parent's number.
WhatsApp offers no official way for another app to act as a person's own account, so the product
depends on how it connects. This research spike (Stage 1 open item) compares the routes. Sources
are listed at the end, read on 2026-11-18. Most are news and vendor blogs; the WhatsApp help pages
and Meta's developer docs are the primary ones.

## Decision
**Option A: a linked-device bridge, running on the kid's tablet.**
- The tablet signs in to the parent's WhatsApp as a linked device. Nothing passes through a server
  of ours, so "messages are never stored on our servers" still holds.
- v1 sends and receives text, voice messages, photos and stickers. **No calls in v1**; calls go
  on the roadmap.
- Parents are told plainly, before setup, that this is not an official WhatsApp feature and that
  WhatsApp may restrict or ban their number for it. No dark patterns around this.
- Keep usage human-paced (one family, a kid's volume of messages) to keep the risk low; never bulk send.

## The finding that changed the brief
**WhatsApp now has its own parent-managed accounts for under-13s.** It was announced on
11 March 2026 and has been rolling out gradually in Brazil since October 2026. A parent aged 18 or
over sets it up with both phones side by side, behind a 6-digit PIN. The parent chooses who can
message the kid and which groups the kid joins, and reviews contact requests from unknown numbers.
The kid gets messaging and calls, with no Meta AI, Channels, Status or ads. Messages stay
end-to-end encrypted, and **parents cannot read them**.

That covers most of FamilyZap's job (allowed contacts, messages and calls, no strangers), for free,
inside WhatsApp itself. What it does not cover, compared with the brief:
- the kid has **their own number and account** (the brief has messages coming from the parent's number);
- parents **cannot see the messages** (the brief has the parent seeing everything);
- **no quiet hours** were reported (to check);
- it needs a phone number for the kid, and a phone or tablet with WhatsApp.

## Options considered
| Option | What it is | Fits the brief? | Risk |
|---|---|---|---|
| A · Linked-device bridge | The tablet signs in to the parent's WhatsApp as an unofficial "linked device" (libraries such as Baileys or whatsapp-web.js) | Yes on paper: parent's number, parent sees all | **Breaks WhatsApp's terms.** The help page says unofficial clients can bring a temporary or permanent ban, including on linking devices. Bans have been reported even for low-volume use. Calls are not realistic. Losing the parent's own WhatsApp is the worst outcome for this audience. |
| B · Official Business Platform (Cloud API) | Meta's official API | No | For business numbers only, not a personal one; templates, fees, business verification. Calling needs a Cloud API business number and user consent. The family would be chatting with a business, not with the parent. |
| C · WhatsApp third-party Agents | The new Agents section lets a user connect up to 5 agents with an API key (limited rollout) | No | Each agent gets one dedicated chat with the user, with no access to other chats or groups. Agent chats are **not end-to-end encrypted** and are stored by the agent's provider, which breaks "no data leaves". |
| D · On-device helper on the parent's Android phone | An app on the parent's phone reads WhatsApp notifications and replies through Android's notification reply and share intents, relaying to and from the tablet | Partly: text and voice relays could work; calls no | Fragile (depends on notifications and Android changes), may still conflict with WhatsApp's terms on automation, and it needs the parent's phone on and nearby. Relaying messages between devices means they pass through a channel we must keep end-to-end encrypted. |
| E · Build on WhatsApp's parent-managed accounts | Accept that the kid has their own WhatsApp account; FamilyZap becomes the warm, simple kid-side experience and setup guide around it (or we drop the WhatsApp part) | Changes the brief: the kid has their own number; the parent doesn't read messages | No terms risk. But WhatsApp's own app may make FamilyZap unnecessary; the product would need a reason to exist beyond it (e.g. quiet hours, a kid-first home with family faces, tablet without SIM). |
| F · Own family messaging | Our own end-to-end encrypted messaging between the kid's tablet and family apps; no WhatsApp | Changes the brief: family must install FamilyZap | No WhatsApp risk, full control (quiet hours, parent view). Biggest build, hardest adoption (grandparents must install an app), competes with Messenger Kids and WhatsApp's own accounts. Product law "no data on our servers" needs relay-only, end-to-end encrypted design. |

## Recommendation given (not followed)
1. **Not A.** It puts the parent's own WhatsApp at risk, and it can't do calls.
2. **First, try WhatsApp's parent-managed accounts on a real family setup** (option E, a one-week test).
   If it does the job, FamilyZap needs a new reason to exist, and Stage 1 is rewritten before we
   name anything.
3. If the test shows a gap worth a product (the parent reading messages, quiet hours, tablets
   without a number, kid-first design), choose between **E** (a companion to WhatsApp) and **F**
   (our own messaging), and rewrite the job to be done around that gap.

## Consequences
- The parent's own WhatsApp number is at risk; the setup flow must say so before linking (Stage 5).
- Google Play may refuse an app that uses WhatsApp unofficially; distribution may need to be outside
  Play (direct APK with self-update). To check before Stage 7.
- WhatsApp's own parent-managed accounts are the main alternative parents will compare us with.
- Stage 2 (strategy and name) can start.
- The name: if WhatsApp isn't in the product (F), "Zap" points at something we aren't; if it is
  (E), it risks looking affiliated with WhatsApp. Either way the name is open in Stage 2.
- The brief's "messages show as the parent" and "parent sees everything" may change.

## Sources
- WhatsApp Help: [About unofficial WhatsApp apps](https://faq.whatsapp.com/1217634902127718)
- WhatsApp Help (pt-BR): [Como configurar uma conta gerenciada pelos pais](https://faq.whatsapp.com/875902238256170/?cms_platform=android&locale=pt_BR)
- Meta for Developers: [Cloud API Calling](https://developers.facebook.com/documentation/business-messaging/whatsapp/calling)
- WhatsApp: [Third-party agents terms](https://www.whatsapp.com/legal/third-party-agents-terms)
- TechCrunch, 2026-03-11: [WhatsApp is launching parent-linked accounts for pre-teens](https://techcrunch.com/2026/03/11/whatsapp-is-launching-parent-linked-accounts-for-pre-teens/)
- MobileBit, 2026-10-09: [WhatsApp para menores chega ao Brasil com contas gerenciadas pelos pais](https://www.mobilebit.com.br/tecnologia/2026/10/09/whatsapp-contas-gerenciadas-brasil/)
- Olhar Digital, 2026-03-12: [WhatsApp para crianças: como criar conta com controle dos pais](https://olhardigital.com.br/2026/03/12/dicas-e-tutoriais/whatsapp-para-criancas-como-criar-conta-com-controle-dos-pais/)
- WABetaInfo: [WhatsApp is rolling out chats with third-party agents](https://wabetainfo.com/whatsapp-is-rolling-out-chats-with-third-party-agents/)
- wasender.dev: [Will an unofficial WhatsApp API get your number banned?](https://wasender.dev/guides/whatsapp-api-ban-risk) (vendor)
- CodeWords: [WhatsApp Business Platform vs unofficial APIs](https://www.codewords.ai/blog/whatsapp-business-api-vs-unofficial-api) (vendor)
