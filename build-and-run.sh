#!/usr/bin/env bash
set -e

# Subir com Docker Compose usando imagens publicadas
docker compose up -d
echo "Aplicação rodando! Acesse via localhost:9999"
