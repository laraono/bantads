#!/bin/bash

set -e

echo " Iniciando o ambiente de microsserviços..."
echo "------------------------------------------"

if ! command -v docker &> /dev/null; then
    echo "Erro: O Docker não está instalado. Por favor, instale-o antes de continuar."
    exit 1
fi

if ! command -v docker compose &> /dev/null; then
    echo "Erro: O Docker Compose não está instalado. Por favor, instale-o antes de continuar."
    exit 1
fi

echo "Realizando o build das imagens..."
echo "------------------------------------------"
docker compose build

echo "Subindo os containers..."
echo "------------------------------------------"
docker compose up -d

echo "Aguardando o API Gateway ficar disponível..."
echo "------------------------------------------"
until curl -sf http://localhost:3000/health > /dev/null; do
    sleep 2
done

curl -X POST --fail --retry 15 --retry-delay 5 --retry-all-errors http://localhost:3000/reboot
echo

echo " Ambiente iniciado com sucesso!"
