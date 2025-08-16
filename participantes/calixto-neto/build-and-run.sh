#!/usr/bin/env bash
set -e

# Pull published images and start services

docker compose -f docker-compose.yml pull
docker compose -f docker-compose.yml up -d
echo "Aplicação rodando! Acesse via localhost:9999"
