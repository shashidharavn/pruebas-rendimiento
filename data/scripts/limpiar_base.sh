#!/bin/sh
PGPASSWORD=$DB_PASSWORD
psql $DB_NAME -U $DB_USERNAME -a -f drop_schemas_sniese.sql
