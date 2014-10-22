#!/bin/sh
PGPASSWORD=$DB_PASSWORD
psql -h $DB_HOST -d $DB_NAME -U $DB_USERNAME -a -f drop_schemas_sniese.sql
