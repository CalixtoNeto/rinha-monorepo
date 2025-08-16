#!/usr/bin/env bash
set -e

# Build Maven artifacts
mvn clean package -DskipTests

# Build local Docker images

docker build -f Dockerfile.backend -t calixtusneto/rinha-backend:latest .
docker build -f Dockerfile.loadbalancer -t calixtusneto/rinha-loadbalancer:latest .

# Start services with Docker Compose

docker compose up -d
echo "Aplicação rodando! Acesse via localhost:9999"
