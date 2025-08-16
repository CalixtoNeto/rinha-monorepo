#!/bin/bash
# Build os mÃ³dulos Maven
mvn clean package -DskipTests
# Build imagens Docker
docker build -f Dockerfile.backend -t rinha-backend:latest .
docker build -f Dockerfile.loadbalancer -t rinha-loadbalancer:latest .
# Subir com Docker Compose
cd participantes/calixto-neto/
docker-compose up -d
echo "AplicaÃ§Ã£o buildada e rodando! Acesse via localhost:80"
