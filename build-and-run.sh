#!/bin/bash
# Build os mÃ³dulos Maven
mvn clean package -DskipTests
# Build imagens Docker
docker build -f Dockerfile.backend -t ghcr.io/calixto-neto/rinha-backend:latest .
docker build -f Dockerfile.loadbalancer -t ghcr.io/calixto-neto/rinha-loadbalancer:latest .
# Subir com Docker Compose
cd participantes/calixto-neto/
docker-compose up -d
echo "Aplicação buildada e rodando! Acesse via localhost:9999"
