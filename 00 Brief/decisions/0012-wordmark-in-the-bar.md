# 0012 · The wordmark's size in the bar

- **Date:** 2026-11-20 · **Status:** proposed, for the owner to confirm · **By:** the design pass (v0.34)

## Context
`01 Brand/logo/README.md` sets the wordmark's minimum at "64 px wide on screen" and its clear space at the i's dot. In the app's 56dp bar both can't hold if "px" is read as dp: a mark 64dp wide is about 48dp tall, which leaves no room for the clear space.

## Decision (proposed)
"px" means physical pixels, as written. The bar's wordmark is 40dp tall and about 52dp wide, which is about 104 px on the kid's tablet (xhdpi or denser) and at least 64 px on any phone sold today (hdpi or denser), with the dot's clear space above and below it. The wordmark file's viewport was trimmed to the mark (the drawing itself is unchanged), so the mark fills its box.

## For the owner
If the minimum was meant as 64dp, the bar has to grow (for example to 72dp) on the screens with the logo. Say so and it's a small change.
