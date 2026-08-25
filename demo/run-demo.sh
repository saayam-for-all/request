#!/usr/bin/env bash
# End-to-end demo of the volunteer notification feature in a fully local,
# controlled environment. Touches no AWS account and no shared Saayam resource.
set -euo pipefail

DEMO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$DEMO_DIR/.." && pwd)"
COMPOSE="docker compose -f $DEMO_DIR/docker-compose.demo.yml"
LOCALSTACK="http://localhost:4566"
QUEUE_NAME="saayam-notifications"
SENDER="no-reply@saayam.test"

banner() { printf '\n\033[1;36m=== %s ===\033[0m\n' "$1"; }

banner "1/5  Starting PostgreSQL + LocalStack"
$COMPOSE up -d

printf 'waiting for postgres  '
until docker exec saayam-notif-db pg_isready -U saayam -d saayam >/dev/null 2>&1; do printf '.'; sleep 2; done
printf ' ready\n'

printf 'waiting for localstack'
until curl -sf "$LOCALSTACK/_localstack/health" >/dev/null 2>&1; do printf '.'; sleep 2; done
printf ' ready\n'

banner "2/5  Seeding notification lookup tables"
# type_id / channel_id are NOT NULL FKs, so these must exist before any
# notification row can be written. Mirrors db/notification_lookup_seed.sql.
docker exec -i saayam-notif-db psql -q -U saayam -d saayam \
    < "$DEMO_DIR/seed-notification-lookups.sql"
docker exec saayam-notif-db psql -U saayam -d saayam -tAc \
    "SELECT channel_name FROM virginia_dev_saayam_rdbms.notification_channels ORDER BY channel_id" \
    | sed 's/^/  channel: /'

banner "3/5  Provisioning SQS queue and SES sender identity in LocalStack"
awslocal() {
    docker exec saayam-notif-localstack \
        awslocal --region us-east-1 "$@"
}
awslocal sqs create-queue --queue-name "$QUEUE_NAME" >/dev/null
awslocal ses verify-email-identity --email-address "$SENDER" >/dev/null
echo "  queue:  $QUEUE_NAME"
echo "  sender: $SENDER (verified)"

banner "4/5  Running the end-to-end test"
cd "$PROJECT_DIR"
mvn -B -q test -Dtest=VolunteerNotificationE2EIT -DfailIfNoTests=false

banner "5/5  Resulting state in the database"
docker exec saayam-notif-db psql -U saayam -d saayam -c "
SELECT n.notification_id,
       n.user_id,
       u.primary_email_address AS email,
       nt.type_name,
       nc.channel_name,
       n.status,
       n.message
FROM virginia_dev_saayam_rdbms.notifications n
JOIN virginia_dev_saayam_rdbms.users u                  ON u.user_id = n.user_id
JOIN virginia_dev_saayam_rdbms.notification_types nt    ON nt.type_id = n.type_id
JOIN virginia_dev_saayam_rdbms.notification_channels nc ON nc.channel_id = n.channel_id
ORDER BY n.notification_id;"

echo
echo "Emails captured by the LocalStack SES emulator:"
curl -s "$LOCALSTACK/_aws/ses" \
  | python3 -c "
import json,sys
data=json.load(sys.stdin)
for m in data.get('messages', []):
    to = ', '.join(m.get('Destination', {}).get('ToAddresses', []))
    subj = m.get('Subject', '')
    print(f'  -> {to}\n     subject: {subj}')
" || echo "  (none)"

banner "Demo complete"
echo "Tear down with:  $COMPOSE down -v"
