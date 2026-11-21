#!/usr/bin/env bash
# How many times each Oiê release was downloaded, from GitHub's public counts (docs/release.md, "Downloads").
# Nothing in the app is measured: this only reads what GitHub already shows for gabrielbr/oie-releases.
#
#   scripts/downloads.sh                    # ask GitHub (no token needed; 60 asks an hour)
#   scripts/downloads.sh --mine 1           # also show the counts without your own N downloads per release
#   scripts/downloads.sh releases.json      # read a saved answer instead (for trying it offline)
#
# Every Oiê updates itself from the same release files, so a version's count mixes new installs and updates.
# About two weeks after a release, the newest version's count is a rough measure of tablets in use.
set -euo pipefail
REPO="gabrielbr/oie-releases"
MINE=0
FILE=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --mine) MINE="${2:?--mine needs a number, like --mine 1}"; shift 2 ;;
    --mine=*) MINE="${1#--mine=}"; shift ;;
    -h|--help) sed -n '2,10p' "$0"; exit 0 ;;
    *) FILE="$1"; shift ;;
  esac
done
[[ "$MINE" =~ ^[0-9]+$ ]] || { echo "--mine needs a whole number, like --mine 1" >&2; exit 2; }

if [[ -n "$FILE" ]]; then
  json=$(cat "$FILE")
else
  json="[]"
  for page in 1 2 3 4 5; do
    part=$(curl -fsSL -H "Accept: application/vnd.github+json" "https://api.github.com/repos/$REPO/releases?per_page=100&page=$page")
    [[ "$part" == "[]" ]] && break
    json=$(python3 -c 'import json,sys; print(json.dumps(json.loads(sys.argv[1]) + json.loads(sys.argv[2])))' "$json" "$part")
  done
fi

python3 - "$json" "$MINE" <<'PY'
import json, sys
releases, mine = json.loads(sys.argv[1]), int(sys.argv[2])
rows = []
for r in releases:
    if r.get("draft"):
        continue
    apks = sum(a.get("download_count", 0) for a in r.get("assets", []) if a.get("name", "").endswith(".apk"))
    rows.append(((r.get("published_at") or "")[:10], r.get("tag_name", "?"), apks))
rows.sort(reverse=True)
total = sum(n for _, _, n in rows)
if mine:
    # Your own downloads come off each release, never below zero.
    print(f"{'Date':<12}{'Version':<12}{'APK downloads':>14}{'Without you':>13}")
    for date, tag, n in rows:
        print(f"{date:<12}{tag:<12}{n:>14}{max(n - mine, 0):>13}")
    others = sum(max(n - mine, 0) for _, _, n in rows)
    print(f"{'':<12}{'Total':<12}{total:>14}{others:>13}")
    print(f"\n{len(rows)} releases; {mine} of your own download(s) taken off each.")
else:
    print(f"{'Date':<12}{'Version':<12}{'APK downloads':>14}")
    for date, tag, n in rows:
        print(f"{date:<12}{tag:<12}{n:>14}")
    print(f"{'':<12}{'Total':<12}{total:>14}")
    print(f"\n{len(rows)} releases. Add --mine 1 to take your own download off each.")
PY
