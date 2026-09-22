# Notification Runtime Setup

The request service publishes request lifecycle notifications to SQS. A Lambda handler consumes those messages and dispatches them through SES for email and SNS for SMS or push targets.

## Required Environment Variables

| Name | Purpose |
| --- | --- |
| `AWS_REGION` | AWS region for SQS, SNS, and SES clients. Defaults to `us-east-1`. |
| `AWS_SQS_NOTIFICATION_QUEUE_URL` | Queue URL used by the request service to enqueue notification events. |
| `AWS_SES_SENDER_EMAIL` | Verified SES sender identity used for email notifications. |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | Optional for local development. In AWS, prefer IAM roles. |

`AWS_SNS_TOPIC_ARN` still exists for compatibility, but direct SMS and push sends use the recipient phone number or endpoint ARN from the notification event.

## Lambda Handler

Use this handler for the SQS consumer Lambda:

```text
org.sfa.request.requesthandler.NotificationQueueHandler
```

Configure the Lambda event source mapping with partial batch response enabled. The handler returns failed message IDs so SQS can retry only failed records.

## IAM Permissions

The application role that enqueues notifications needs:

```text
sqs:SendMessage
```

The notification Lambda role needs:

```text
sqs:ReceiveMessage
sqs:DeleteMessage
sqs:GetQueueAttributes
ses:SendEmail
sns:Publish
```

Scope each permission to the concrete queue, SES identity, and SNS resources when deploying.

## Queue And Retry Setup

Create an SQS queue for notification events and configure a dead-letter queue. Set the main queue redrive policy so messages move to the DLQ after the chosen retry count.

The Lambda handler reports partial batch failures; AWS handles retry and DLQ movement through the event source mapping and queue redrive policy.

## SES Notes

Verify `AWS_SES_SENDER_EMAIL` in SES before sending. If the SES account is still in sandbox mode, recipient email addresses must also be verified.

## Database Requirements

The in-app channel writes to the shared `notifications` table. Two things must be true
before it will work:

1. **Lookup tables must be seeded.** `notifications.type_id` and
   `notifications.channel_id` are NOT NULL foreign keys to `notification_types` and
   `notification_channels`, and neither table ships with seed data. Apply
   `db/notification_lookup_seed.sql` (vetted by the DBMS team) first. The `IN_APP`
   channel row is new and is required by this feature.

2. **The schema must be on the connection search_path.** `notifications.status` is the
   PostgreSQL enum `status_type`, which is created inside the regional schema. The
   entity casts writes to that enum by unqualified name, so the connection must resolve
   it. Set either:

   ```properties
   spring.datasource.hikari.connection-init-sql=SET search_path TO virginia_dev_saayam_rdbms, public
   ```

   or append `?currentSchema=virginia_dev_saayam_rdbms` to the JDBC URL. Use the
   schema for the region being deployed.

## Running Against a Local AWS Emulator

The service can be pointed at a local emulator instead of AWS by setting
`AWS_ENDPOINT_OVERRIDE` (for example `http://localhost:4566`). When that property is
set, static dummy credentials are used, because emulators require credentials to be
present but do not validate them. Leave it empty in every deployed environment so the
real regional endpoints and the IAM role are used.

The emulator must have the notification queue created and the sender identity verified
before the service starts, and the lookup tables from `db/notification_lookup_seed.sql`
must be present in the database.

## Local Verification

Compile without running tests:

```powershell
.\mvnw.cmd -DskipTests compile
```

Run only the notification tests:

```powershell
.\mvnw.cmd -Dtest=Notification*Test,EmailNotificationServiceImplTest,SnsNotificationServiceImplTest test
```
