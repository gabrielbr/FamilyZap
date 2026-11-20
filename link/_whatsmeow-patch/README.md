# whatsmeow, modified (one file)

`broadcast.go` is whatsmeow's own file (github.com/tulir/whatsmeow, at the commit pinned in `../go.mod`),
modified by Oiê on 2026-11-20 (v0.37). It stays under the **Mozilla Public License 2.0**.

The change: `SetStatusRecipients` lets a status go to a chosen list of people (the kid's Contatos) instead of
the account's status privacy list. See the comments marked "Oiê patch" and `00 Brief/decisions/0003-whatsmeow.md`.

`../patch-whatsmeow.sh` copies the pinned whatsmeow into `../.whatsmeow/` and puts this file in; `../go.mod`
replaces the module with that copy. Every release publishes this file as `whatsmeow-broadcast.go`.
