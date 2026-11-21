# Oiê

A kid aged 9–12 messages and calls family from an Android tablet through a parent's WhatsApp, only
with contacts the parent allowed.

Built with the Gabriel Product Boilerplate playbook (track B). Where things stand: [STATUS.md](STATUS.md).
What the product is: [PRODUCT.md](PRODUCT.md). The brief: [00 Brief/brief.md](00%20Brief/brief.md).
Agents start at [AGENTS.md](AGENTS.md).

## Design
`python3 design/build.py --check` checks the tokens (it runs in CI). See [design/README.md](design/README.md).

## The app (v0.1)
An Android app for the kid's tablet: Jetpack Compose, `app.oie`, min Android 8. The kid side, the setup, the PIN and the parent area all work with **sample family data**. There is **no real WhatsApp connection yet** (`app/src/main/java/app/oie/data/WhatsAppLink.kt`).

```sh
./gradlew assembleDebug            # app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest        # the message rule, quiet hours, PIN
./gradlew verifyRoborazziDebug     # screenshots of the real screens, light and dark
```
Needs JDK 17+ and the Android SDK (platform 35). The logic lives in `app/src/main/java/app/oie/logic/`, with tests in `app/src/test/`.


How many times each release was downloaded, from GitHub's public counts (no analytics in the app; see `docs/release.md`, "Downloads"):
```sh
scripts/downloads.sh            # add --mine 1 to leave out your own download of each release
```
