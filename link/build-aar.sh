#!/usr/bin/env bash
# Builds link/oielink.aar for the app (arm64 and armv7). Needs Go (the version in go.mod is fetched
# automatically), the Android SDK and NDK. The app build needs it (app/build.gradle.kts); CI builds it first.
set -euo pipefail
cd "$(dirname "$0")"
export GOTOOLCHAIN=${GOTOOLCHAIN:-auto}
export PATH="$PATH:$(go env GOPATH)/bin"
go install golang.org/x/mobile/cmd/gomobile@latest golang.org/x/mobile/cmd/gobind@latest
"$(go env GOPATH)/bin/gomobile" init
"$(go env GOPATH)/bin/gomobile" bind -target=android/arm64,android/arm -androidapi 26 -ldflags="-s -w" -o oielink.aar .
ls -la oielink.aar
