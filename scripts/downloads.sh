#!/usr/bin/env bash
# How many times each Oiê release was downloaded, from GitHub's public counts (docs/release.md, "Downloads").
# Nothing in the app is measured: this only reads what GitHub already shows for gabrielbr/oie-releases.
#
#   scripts/downloads.sh                 # ask GitHub (no token needed; 60 asks an hour)
#   scripts/downloads.sh releases.json   # read a saved answer instead (for trying it offline)
#
# Every Oiê updates itself from the same release files, so a version's count mixes new installs and updates.
# About two weeks after a release, the newest version's count is a rough measure of tablets in use.
set -euo pipefail
REPO="gabrielbr/oie-releases"

if [[ $# -ge 1 ]]; then
  json=$(cat "$1")
else
  json="[]"
  for page in 1 2 3 4 5; do
    part=$(curl -fsSL -H "Accept: application/vnd.github+json" "https://api.github.com/repos/$REPO/releases?per_page=100&page=$page")
    [[ "$part" == "[]" ]] && break
    json=$(python3 -c 'import json,sys; print(json.dumps(json.loads(sys.argv[1]) + json.loads(sys.argv[2])))' "$json" "$part")
  done
fi

python3 - "$json" <<'PY'
import json, sys
releases = json.loads(sys.argv[1])
rows, total = [], 0
for r in releases:
    if r.get("draft"):
        continue
    apks = sum(a.get("download_count", 0) for a in r.get("assets", []) if a.get("name", "").endswith(".apk"))
    total += apks
    rows.append(((r.get("published_at") or "")[:10], r.get("tag_name", "?"), apks))
rows.sort(reverse=True)
print(f"{'Date':<12}{'Version':<12}{'APK downloads':>14}")
for date, tag, n in rows:
    print(f"{date:<12}{tag:<12}{n:>14}")
print(f"{'':<12}{'Total':<12}{total:>14}")
PY
