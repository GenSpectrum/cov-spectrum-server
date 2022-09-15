#!/usr/bin/bash
set -euo pipefail

cd /app/
python main.py \
  --db-host $DB_HOST \
  --db-port $DB_PORT \
  --db-name $DB_NAME \
  --db-user $DB_USER \
  --db-password $DB_PASSWORD \
  --notification-key $NOTIFICATION_KEY
