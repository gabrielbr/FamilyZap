# 0008 · Lottie, for WhatsApp's own animated stickers

- **Date:** 2026-11-20
- **Status:** approved
- **Approved by:** Owner ("Sim, adicionar", 2026-11-20)

## Context
The owner's test: animated stickers from WhatsApp's own packs showed still. Those stickers are Lottie animations ("application/was"), which Android can't draw.

## Decision
- Add Airbnb's **Lottie** for Compose (`com.airbnb.android:lottie-compose`, Apache License 2.0), pinned at 6.6.0 to match the app's Compose and Kotlin.
- Credited in Sobre.

## Options considered
| Option | Why not |
|---|---|
| Show them still | The owner wants them to move. |
| Our own Lottie renderer | Out of proportion. |

## Consequences
- The app grows by about 0.5 MB.
- A saved Lottie sticker goes back out as Lottie: the link sets `IsLottie` and the "application/was" type when the file is a zip.
