#!/bin/bash
# Runs once, on first initialization of the PostgreSQL data volume
# Creates one database per data-owning service (idempotent)
set -euo pipefail

DATABASES="account_db admin_db atm_db card_db cash_db customer_db transaction_db"

for db in $DATABASES; do
  echo "Ensuring database '$db' exists..."
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT 'CREATE DATABASE $db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db')\gexec
EOSQL
done