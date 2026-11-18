# Changelog

## Unreleased
- **Updates without a tag:** merging a version bump to `main` now publishes the release by itself, so the tablet offers it at the next check. Release notes come from this file's `## <version>` section (`docs/release.md`).

## 0.4.0
- Receiving (flow "Kid receives messages").
- **One way in:** every message from the WhatsApp link goes through `Inbox.decide`. Messages from chats that aren't allowed, and the parent's private messages, are **never stored** on the tablet. The rest is shown.
- **Notifications:** Oiê's own channel, one per family member, with the kind in words ("Mandou um áudio"). Tapping one opens that chat. Permission is asked at the end of setup, after saying why.
- **Quiet hours:** messages still arrive quietly, so the kid can read them, and become one summary notification when the period ends.
- **Delete for everyone:** the message turns into "Mensagem apagada" and its photo is dropped.
- **Test only:** "Simular mensagem da Vovó (teste)" in Conexão, until the real link exists.
- 59 tests.

## 0.3.0
- Self-update (the Gabriel Design System's rule, `docs/self-update.md`).
- **The check:** on every return to the foreground, at most every 15 minutes, with ETag. It asks GitHub's public API for the latest release and sends nothing about the family.
- **Install rules:** only a newer, non-prerelease release with an `Oie-*.apk` and `SHA256SUMS.txt` is offered. The download installs only if it's complete, its SHA-256 matches, and it's the same package signed with the same key. Then Android asks to confirm.
- **Where it shows:** the "Atualização disponível" sheet appears **only in the parent area**, not to the kid (proposed, for the owner to confirm). About has the version and "Procurar atualizações". A debug copy says it can't update itself.
- **New permissions:** `INTERNET` and `REQUEST_INSTALL_PACKAGES`, for self-update only.
- **Releases:** `.github/workflows/release.yml` builds a signed APK and `SHA256SUMS.txt` on a `v*` tag, once the owner adds the signing secrets (`docs/release.md`).
- **Tests:** portrait screenshots and the update rules (version compare, offer, sums, checks, throttle, sizes). 48 tests.
- **Owner decisions, built:**
  - "Pedir para adicionar alguém" on the kid's empty home. The parent sees "{kid} pediu para adicionar alguém" in Contatos. No names and no typing, so it can't reach anyone.
  - Self-update reads the public `gabrielbr/oie-releases`, while the code stays private.
  - The update sheet stays in the parent area only (design system divergence D-14).
  - The six sticker words are approved.

## 0.2.0
- **Sticker images:** the six stickers are real images. Their words are outlined with Nunito (`_02` masters) and converted to Android vectors by `tools/svg2vector.py`.
- **Quiet hours in the parent area:** add, edit (name, days, start and end with a time picker) and delete with undo.
- **Ajuda e sobre:** short answers to eight questions the flows raise, the risk notice repeated, version, "not made or approved by WhatsApp or Meta", and the licences.
- **Photo files encrypted:** each photo is sealed under its own Keystore key.
- **English:** every word lives in one typed `Strings` with Portuguese (the source) and English, picked from the device or per-app language. Tests check the voice rules (no "!", no em dash, no Zap/Wpp) in both languages.

## 0.1.0
- An Android app (`app/`, Jetpack Compose, `app.oie`) for the kid's tablet. It has setup with the risk notice, a PIN, the kid home and conversation (text, hold-to-record voice, stickers, 5 s undo), and the parent area (contacts with undo, quiet-hours exceptions, connection, about). It works only with **sample data**: there is no real WhatsApp link yet. The message rule, quiet hours and PIN rules are tested, Roborazzi screenshot tests cover the screens, and CI builds and tests on every PR. Nunito is bundled (OFL, `licenses/`).
- **Photo sending:** camera or gallery through Android's own camera app and photo picker, so Oiê asks for no permission. The last source used comes first. A preview offers Trocar and Enviar, undo returns to the preview, and the photo shows in the chat. Photos are re-encoded on the way in (at most 1600 px), which drops the camera's metadata, including location.
- **Saving on the tablet:** the whole state (setup, names, the PIN hash, contacts, quiet hours, messages, drafts) is saved as it changes. It is sealed with AES-GCM under a key kept in the Android Keystore, in the app's private storage; nothing goes to a server. Setup resumes at the same step; the parent area always asks for the PIN again. A damaged file starts fresh instead of crashing.
- Tests: 30 in total (6 message rule, 7 quiet hours and PIN, 4 saving, 13 screenshots). The screenshots use a pinned clock and time zone so CI can compare them.

## Before the app
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
