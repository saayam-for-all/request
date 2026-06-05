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

## Local Verification

Compile without running tests:

```powershell
.\mvnw.cmd -DskipTests compile
```

Run only the notification tests:

```powershell
.\mvnw.cmd -Dtest=Notification*Test,EmailNotificationServiceImplTest,SnsNotificationServiceImplTest test
```
