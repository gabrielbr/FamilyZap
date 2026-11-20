# 0011 · The conversation uses the tablet's width

- **Date:** 2026-11-20 · **Status:** accepted · **By:** the design pass (v0.34)

## Context
AGENTS.md and the design system say "one column up to 600dp". The v0.34 review pointed out that the chat's list runs the full width of the tablet.

## Decision
The conversation is the one screen that uses the whole width: the family's messages sit on the left and the kid's on the right, as in every messaging app the family knows. Each bubble keeps the 600dp maximum, so no line gets longer than that. Every other screen stays a single column up to 600dp.

## Consequences
Long messages wrap at 600dp; the two sides stay apart on the tablet, which makes who-said-what clear at a glance.
