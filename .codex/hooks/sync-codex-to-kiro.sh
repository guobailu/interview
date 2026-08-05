#!/bin/sh

set -eu

repository_root=$(git rev-parse --show-toplevel 2>/dev/null) || exit 0
codex_skills="$repository_root/.codex/skills"
kiro_skills="$repository_root/.kiro/skills"

if [ ! -d "$codex_skills" ]; then
  exit 0
fi

mkdir -p "$kiro_skills"

if /usr/bin/diff -qr -x .DS_Store "$codex_skills" "$kiro_skills" >/dev/null 2>&1; then
  exit 0
fi

/usr/bin/rsync \
  --archive \
  --delete \
  --exclude=.DS_Store \
  "$codex_skills/" \
  "$kiro_skills/"

printf '%s\n' 'Synchronized .codex/skills to .kiro/skills.'
