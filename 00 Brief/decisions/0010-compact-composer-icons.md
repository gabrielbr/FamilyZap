# 0010 · The composer's icons go without words on a phone

- **Date:** 2026-11-20 · **Status:** accepted · **By:** the design pass (v0.34), within the owner's standing rules

## Context
Design System §9 says icons on the kid side always carry a word. In the conversation's composer on a phone (360dp) or in a chat bubble, the camera, sticker and send buttons with words leave the text field too little room (v0.29 audit, P1).

## Decision
On a tablet the composer's buttons show their word ("Foto", "Figurinha", "Enviar"). On a narrow screen (under 600dp) they show the icon alone, with the word as their TalkBack label, and the camera and stickers step aside while the kid types.

## Consequences
This is the one exception to §9's "always with a word". Every other kid-side icon keeps its word.
