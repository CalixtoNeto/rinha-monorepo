#!/usr/bin/env bash
set -e

# Subir com Docker Compose usando build local
docker compose up -d --build
echo "Aplicação buildada e rodando! Acesse via localhost:9999"
