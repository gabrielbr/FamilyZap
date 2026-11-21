# Releasing Oiê

A release is what self-update offers (Gabriel Design System `docs/self-update.md`): a signed APK named `Oie-<version>.apk` and `SHA256SUMS.txt`, published together as a GitHub release in the public `gabrielbr/oie-releases`. The app installs an update only when it's newer and complete, its SHA-256 is in `SHA256SUMS.txt`, and it's signed with the same key as the installed app.

## Once (owner only)
1. **The signing key.** Create it, and keep it and its passwords somewhere safe outside the repo. If it's lost, installed apps can never update again.
   ```sh
   keytool -genkeypair -v -keystore oie.jks -alias oie -keyalg RSA -keysize 4096 -validity 10000
   ```
2. **The four signing secrets.** Add them in this repo under **Settings → Secrets and variables → Actions**:
   - `OIE_KEYSTORE_BASE64`: the output of `base64 -w0 oie.jks`;
   - `OIE_KEYSTORE_PASSWORD`;
   - `OIE_KEY_ALIAS` (`oie`);
   - `OIE_KEY_PASSWORD`.
3. **The releases repo.** Create the **public** repo `gabrielbr/oie-releases`, and tick **Add a README**: a release needs at least one commit to tag. Self-update reads it through GitHub's public API, while the code stays private here (owner's decision, 2026-11-18).
4. **The token.** Create a fine-grained GitHub token with **Contents: read and write** on `gabrielbr/oie-releases` only. Add it here as the secret `OIE_RELEASES_TOKEN`.
5. **The first install.** Uninstall any debug copy from the tablet. A debug copy is signed with a different key, so Android won't put a release over it. Then install `Oie-<version>.apk` from the releases page. From then on, the app updates itself.

Until the secrets exist, the `release` workflow only warns and skips.

## Building
The app needs the WhatsApp link library: `link/build-aar.sh` builds it, with Go and the Android NDK. The workflows build it before Gradle.

## Assets
Each release has:
- `Oie-<version>.apk`: universal, for manual installs and for copies of Oiê older than 0.6;
- `Oie_arm64-v8a_<version>.apk` and `Oie_armeabi-v7a_<version>.apk`: one per architecture, about half the size. Self-update picks the tablet's own;
- `SHA256SUMS.txt`, covering all of them.

## Each release
1. In the PR, bump `versionCode` and `versionName` in `app/build.gradle.kts`, and `BuildConfigInfo.VERSION` to match.
2. Move the CHANGELOG's notes from `## Unreleased` to a `## <version>` heading. They become the notes in the update sheet, as plain text, so write them for the parent.
3. Merge to `main`. The `release` workflow:
   - sees that the version isn't released;
   - runs the tests;
   - builds and signs the APK and writes `SHA256SUMS.txt`;
   - publishes the release;
   - tags the commit `v<version>`.

   A merge that doesn't bump the version releases nothing.
4. On the tablet, the parent area offers the update at the next check, or right away from **Ajuda › Sobre › Procurar atualizações**.

You can also run the workflow by hand (**Actions › release › Run workflow**). It does the same check, so running it twice doesn't publish twice.

## Downloads
`scripts/downloads.sh` prints one line per release (date, version, APK downloads) and the total, from GitHub's public counts for `gabrielbr/oie-releases`. It needs `curl` and `python3`, and no token.
- **No analytics:** nothing in the app is measured; it only reads what GitHub already shows on the releases page.
- **Your own downloads:** `scripts/downloads.sh --mine 1` adds a "Without you" column, with your one download per release taken off (use the number of downloads you make per release). The last line says how many releases there are.
- **Not people:** a count is downloads, not people. Every Oiê updates itself from the same files, so a version's count mixes new installs and updates (and a few bots and repeats).
- **Rough tablets in use:** about two weeks after a release, the newest version's count is a rough measure of tablets in use.
- **The website:** GitHub Pages keeps no visitor numbers, so the site stays unmeasured.

## Testing between releases
A debug copy (the CI artifact on each PR) can't update itself, and its About says so. To test a change on a tablet that runs a release, merge the change with a version bump instead.
