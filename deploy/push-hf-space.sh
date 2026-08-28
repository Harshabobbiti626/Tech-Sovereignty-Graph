#!/bin/sh
# Push this repo to a Hugging Face Docker Space. The Space repo gets the same
# tree, except README.md is swapped for the Space frontmatter version.
#
# Usage:
#   HF_USER=<hf-username> HF_SPACE=<space-name> HF_TOKEN=<write token> \
#     ./deploy/push-hf-space.sh
set -eu
: "${HF_USER:?set HF_USER}" "${HF_SPACE:?set HF_SPACE}" "${HF_TOKEN:?set HF_TOKEN}"
REMOTE="https://${HF_USER}:${HF_TOKEN}@huggingface.co/spaces/${HF_USER}/${HF_SPACE}"
BRANCH="hf-space-push"

git checkout -b "$BRANCH"
cp deploy/space-README.md README.md
git add README.md
git commit -m "hf space metadata" --no-verify
# force: each push is a fresh deploy of the current tree
git push -f "$REMOTE" "$BRANCH:main"
git checkout -
git branch -D "$BRANCH"

echo "Pushed. Build logs: https://huggingface.co/spaces/${HF_USER}/${HF_SPACE}"
