#!/usr/bin/env bash
set -e

# Build Maven modules using all CPU cores
mvn -T 1C clean package -DskipTests

# Build Docker images in parallel for faster builds
docker build -f Dockerfile.backend -t ghcr.io/calixto-neto/rinha-backend:latest . &
docker build -f Dockerfile.loadbalancer -t ghcr.io/calixto-neto/rinha-loadbalancer:latest . &
wait

# Subir com Docker Compose
cd participantes/calixto-neto/
docker-compose up -d
echo "Aplicação buildada e rodando! Acesse via localhost:9999"
