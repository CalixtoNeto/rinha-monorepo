#!/bin/bash
set -e

# Build Maven modules
mvn clean package -DskipTests

# Build Docker images tagged for Docker Hub
IMAGE_USER=${DOCKERHUB_USERNAME:-calixto-neto}

docker build -f Dockerfile.backend -t "$IMAGE_USER/rinha-backend:latest" .
docker build -f Dockerfile.loadbalancer -t "$IMAGE_USER/rinha-loadbalancer:latest" .

# Start stack with Docker Compose
cd participantes/calixto-neto/
docker-compose up -d

echo "Aplicação buildada e rodando! Acesse via localhost:9999"
