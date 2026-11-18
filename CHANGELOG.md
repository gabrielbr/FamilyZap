# Changelog

## Unreleased
- Project started from the Gabriel Product Boilerplate.
- Stage 1: keywords (warm, simple, trustworthy, close) and references (Headspace, Duolingo) added to the brief.
- Stage 1: research on how to reach WhatsApp, with a decision (0001): linked-device bridge on the kid's tablet, no calls in v1. Stage 1 done.
- Stage 2 done: strategy (Family, within reach · Send love home) and the name Oiê (decision 0002), replacing the working name FamilyZap. Project code is now OIE.
- Stage 3: three logo directions; direction C (Close) approved; logo pack in `01 Brand/logo/` in coral #E8573C.
- Stage 3: voice (`01 Brand/voice.md`) and imagery and icons (`01 Brand/imagery-and-icons.md`). Stage 3 done.
- Stage 4: Oiê builds on the Gabriel Design System core 0.31.1 (`design/`), with its own brand (coral, Nunito) and layer. Generated `OTokens.kt` and `tokens.css`; design system README and AGENTS.md brand rules filled. Stage 4 done.
- Stage 5: flow "Kid sends a message" (`docs/flows/kid-sends-message.md`). Icons now follow the design system's line set.
- Stage 5: flow "Parent setup + link" (`docs/flows/parent-setup-link.md`). Oiê is now one app on the kid's tablet with a parent area behind a PIN; the parent's phone only scans the QR in WhatsApp.
- Stage 5: flows "Choose contacts + quiet hours" and "Kid receives messages"; tablet prototype in `03 Product/prototype/` (8 screens, light and dark); tokens `qr-ground`/`qr-ink`.
- Stage 6: the full five-skill audit of the v0.1 prototype.
- Owner decisions: the message rule (to test), PIN wait, idle lock, presets, groups. Stages 5 and 6 done.
- Prototype v0.2: 13 more screens (PIN, setup 3–6, done, add contact, recording, photo preview, stickers, undo, offline, link dropped), built by `03 Product/prototype/build.py`; 6 proposed stickers in `01 Brand/stickers/`; the core icons Oiê uses are vendored in `design/core/icons/` (core 0.32.0).
- **App v0.1:** an Android app (`app/`, Jetpack Compose, `app.oie`) for the kid's tablet. It has setup with the risk notice, a PIN, the kid home and conversation (text, hold-to-record voice, stickers, 5 s undo), and the parent area (contacts with undo, quiet-hours exceptions, connection, about). It works only with **sample data**: there is no real WhatsApp link yet. The message rule, quiet hours and PIN rules are tested, Roborazzi screenshot tests cover the screens, and CI builds and tests on every PR. Nunito is bundled (OFL, `licenses/`).
- **Photo sending:** camera or gallery through Android's own camera app and photo picker, so Oiê asks for no permission. The last source used comes first. A preview offers Trocar and Enviar, undo returns to the preview, and the photo shows in the chat. Photos are re-encoded on the way in (at most 1600 px), which drops the camera's metadata, including location.
- **Saving on the tablet:** the whole state (setup, names, the PIN hash, contacts, quiet hours, messages, drafts) is saved as it changes. It is sealed with AES-GCM under a key kept in the Android Keystore, in the app's private storage; nothing goes to a server. Setup resumes at the same step; the parent area always asks for the PIN again. A damaged file starts fresh instead of crashing.
- Tests: 30 in total (6 message rule, 7 quiet hours and PIN, 4 saving, 13 screenshots). The screenshots use a pinned clock and time zone so CI can compare them.
