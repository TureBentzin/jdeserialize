#!/usr/bin/env bash

docker login registry.git.fh-aachen.de
docker build -t registry.git.fh-aachen.de/tb3838s/jdeserialize/ant-builder .
docker push registry.git.fh-aachen.de/tb3838s/jdeserialize/ant-builder
