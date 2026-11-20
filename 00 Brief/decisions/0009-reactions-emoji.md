# 0009 · Reactions use emoji

- **Date:** 2026-11-20 · **Status:** accepted · **By:** the owner (asked for reactions, v0.33)

## Context
The voice (01 Brand/voice.md) and AGENTS.md say "no emoji in UI text" and "never emoji as icons". Reactions in WhatsApp are emoji: they are what the family sends and sees.

## Decision
The six reactions (❤️ 😂 😮 😢 👍 🙏) and the family's reactions are shown as emoji, because they are message content, like the emoji in a family member's text. Oiê's own interface, labels, buttons and icons still use no emoji; each reaction has a label for TalkBack ("Reagir com ❤️").

## Consequences
The emoji come from the tablet's emoji font, not from the design system. They appear only in the reactions sheet and in the chips under messages.
