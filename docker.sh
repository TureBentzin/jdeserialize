#!/usr/bin/env bash

read -rp "Enter image version tag: " VERSION
docker login registry.git.fh-aachen.de
docker build -t registry.git.fh-aachen.de/tb3838s/jdeserialize/ant-builder:"$VERSION" .
docker push registry.git.fh-aachen.de/tb3838s/jdeserialize/ant-builder:"$VERSION"

read -rp "Also tag as 'latest'? (y/N): " LATEST
if [[ "$LATEST" =~ ^[Yy]$ ]]; then
  docker tag registry.git.fh-aachen.de/tb3838s/jdeserialize/ant-builder:"$VERSION" \
             registry.git.fh-aachen.de/tb3838s/jdeserialize/ant-builder:latest
  docker push registry.git.fh-aachen.de/tb3838s/jdeserialize/ant-builder:latest
fi

read -rp "Update .gitlab-ci.yml with new version tag? (y/N): " UPDATE_CI

if [[ "$UPDATE_CI" =~ ^[Yy]$ ]]; then
  sed -i.bak -E "s/(image: registry.git.fh-aachen.de\/tb3838s\/jdeserialize\/ant-builder:).*/\1$VERSION/" .gitlab-ci.yml
  rm .gitlab-ci.yml.bak
  echo ".gitlab-ci.yml updated with new version tag."
fi

echo "Done."
