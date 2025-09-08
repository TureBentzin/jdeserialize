#!/usr/bin/env bash

DRY_RUN=false
if [[ "$1" == "--dry" ]]; then
  DRY_RUN=true
  echo "[DRY RUN] Push commands will be skipped."
fi

read -rp "Enter image version tag: " VERSION
docker login registry.git.fh-aachen.de
docker build -t registry.git.fh-aachen.de/tb3838s/jdeserialize/ant-builder:"$VERSION" .

if [ "$DRY_RUN" = false ]; then
  docker push registry.git.fh-aachen.de/tb3838s/jdeserialize/ant-builder:"$VERSION"
else
  echo "[DRY RUN] Skipped: docker push ...:$VERSION"
fi

read -rp "Also tag as 'latest'? (y/N): " LATEST
if [[ "$LATEST" =~ ^[Yy]$ ]]; then
  docker tag registry.git.fh-aachen.de/tb3838s/jdeserialize/ant-builder:"$VERSION" \
             registry.git.fh-aachen.de/tb3838s/jdeserialize/ant-builder:latest
  if [ "$DRY_RUN" = false ]; then
    docker push registry.git.fh-aachen.de/tb3838s/jdeserialize/ant-builder:latest
  else
    echo "[DRY RUN] Skipped: docker push ...:latest"
  fi
fi

read -rp "Update .gitlab-ci.yml with new version tag? (y/N): " UPDATE_CI
if [[ "$UPDATE_CI" =~ ^[Yy]$ ]]; then
  sed -i.bak -E "s/(image: registry.git.fh-aachen.de\/tb3838s\/jdeserialize\/ant-builder:).*/\1$VERSION/" .gitlab-ci.yml
  if ! cmp -s .gitlab-ci.yml .gitlab-ci.yml.bak; then
    echo "Updated .gitlab-ci.yml with version $VERSION"
    diff .gitlab-ci.yml.bak .gitlab-ci.yml
    rm .gitlab-ci.yml.bak
  else
    echo "No substitution made — aborting." >&2
    mv .gitlab-ci.yml.bak .gitlab-ci.yml
    exit 1
  fi
  echo ".gitlab-ci.yml updated with new version tag."
fi

echo "Done."
