#!/bin/bash
set -e

create_db_and_schema() {
    local db=$1
    local sql_file=$2
    echo "Creating database: $db"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -c "CREATE DATABASE $db;"
    
    if [ -f "$sql_file" ]; then
        echo "Applying schema to $db from $sql_file"
        psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$db" -f "$sql_file"
    else
        echo "ERROR: Schema file $sql_file not found!"
    fi
}

create_db_and_schema "command_db" "/docker-entrypoint-initdb.d/schemas/command.sql"
create_db_and_schema "request_db" "/docker-entrypoint-initdb.d/schemas/request.sql"
create_db_and_schema "manager_db" "/docker-entrypoint-initdb.d/schemas/manager.sql"
create_db_and_schema "client_db" "/docker-entrypoint-initdb.d/schemas/client.sql"