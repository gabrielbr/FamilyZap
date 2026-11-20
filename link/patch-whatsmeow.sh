#!/usr/bin/env bash
# Oiê builds whatsmeow with one modified file, _whatsmeow-patch/broadcast.go (v0.37, decision 0003):
# a status goes only to the people in Contatos, never to the parent's whole status audience.
# This copies the pinned whatsmeow into .whatsmeow/ (not committed) and puts the modified file in.
# go.mod points at it with a replace. The modified file is published here under the MPL-2.0.
set -euo pipefail
cd "$(dirname "$0")"
export GOTOOLCHAIN=${GOTOOLCHAIN:-auto}
ver=$(grep -oP 'go\.mau\.fi/whatsmeow \Kv[^ ]+' go.mod | head -1)
src=$(GOFLAGS=-mod=mod go mod download -json "go.mau.fi/whatsmeow@$ver" | grep -oP '"Dir": "\K[^"]+')
if [ ! -f .whatsmeow/.version ] || [ "$(cat .whatsmeow/.version)" != "$ver" ] || ! cmp -s _whatsmeow-patch/broadcast.go .whatsmeow/broadcast.go; then
  rm -rf .whatsmeow
  cp -r "$src" .whatsmeow
  chmod -R u+w .whatsmeow
  cp _whatsmeow-patch/broadcast.go .whatsmeow/broadcast.go
  echo "$ver" > .whatsmeow/.version
fi
