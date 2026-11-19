//go:build tools

// Keeps golang.org/x/mobile in go.mod: gomobile bind needs it (build-aar.sh).
package oielink

import _ "golang.org/x/mobile/bind"
