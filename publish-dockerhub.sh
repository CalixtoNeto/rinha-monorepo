#!/bin/bash
set -euo pipefail

# Só publica se houver alteração no pom.xml
if git diff --quiet HEAD -- pom.xml; then
  echo "pom.xml unchanged, skipping Docker Hub publish."
  exit 0
fi

: "${DOCKERHUB_USERNAME:?}"
: "${DOCKERHUB_TOKEN:?}"

# Build Maven artifacts
mvn clean package -DskipTests

# Build images with Docker Hub tags
docker build -f Dockerfile.backend -t "$DOCKERHUB_USERNAME/rinha-backend:latest" .
docker build -f Dockerfile.loadbalancer -t "$DOCKERHUB_USERNAME/rinha-loadbalancer:latest" .

# Authenticate and push images
echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin
docker push "$DOCKERHUB_USERNAME/rinha-backend:latest"
docker push "$DOCKERHUB_USERNAME/rinha-loadbalancer:latest"
