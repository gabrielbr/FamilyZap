# Releasing Oiê

A release is what self-update offers (Gabriel Design System `docs/self-update.md`): a signed APK named `Oie-<version>.apk` and `SHA256SUMS.txt`, published together as a GitHub release. The app installs an update only when it is newer, complete, its SHA-256 is in `SHA256SUMS.txt`, and it is signed with the same key as the installed app.

## Once: the signing key (owner only)
1. Create a key, and keep it and its passwords somewhere safe outside the repo. Losing it means installed apps can never update again.
   ```sh
   keytool -genkeypair -v -keystore oie.jks -alias oie -keyalg RSA -keysize 4096 -validity 10000
   ```
2. In the GitHub repo's **Settings → Secrets and variables → Actions**, add:
   - `OIE_KEYSTORE_BASE64`: the output of `base64 -w0 oie.jks`;
   - `OIE_KEYSTORE_PASSWORD`;
   - `OIE_KEY_ALIAS` (`oie`);
   - `OIE_KEY_PASSWORD`.
3. Create an empty **public** repo, `gabrielbr/oie-releases`. Self-update reads its releases through GitHub's public API, while the code stays private here (owner's decision, 2026-11-18).
4. Create a fine-grained GitHub token with **Contents: read and write** on `gabrielbr/oie-releases` only, and add it here as the secret `OIE_RELEASES_TOKEN`.

## Each release
1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`, and `BuildConfigInfo.VERSION` to match.
2. Move the CHANGELOG's notes under a `## <version>` heading. They become the release notes in the update sheet.
3. Merge to `main`, then tag: `git tag v0.3.0 && git push origin v0.3.0`.
4. The `release` workflow tests, builds the signed APK, writes `SHA256SUMS.txt`, and publishes both to `gabrielbr/oie-releases`.

Debug builds (the CI artifact) are signed with a debug key, so they can't update themselves. Their About says so.
