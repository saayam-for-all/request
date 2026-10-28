-- ============================================================================
-- Notification lookup seed data
--
-- OWNERSHIP NOTE: the `database` repo owns canonical DDL/DML. This file is the
-- Request microservice's concrete requirement, to be handed to the DBMS team for
-- vetting per the "Changes to the Database, Waiting for Microservice" process --
-- it is NOT to be applied to a shared environment unilaterally.
--
-- WHY THIS IS REQUIRED: notifications.type_id and notifications.channel_id are
-- NOT NULL foreign keys to these two lookup tables, and neither table has any
-- committed seed data. Until they are seeded, no notification row can be
-- inserted at all.
--
-- IN_APP is a new channel value. The existing DDL comment set implies
-- EMAIL/SMS/PUSH only; the in-app notification centre needs its own channel so
-- that user_notification_preferences can distinguish it from outbound channels.
-- ============================================================================

INSERT INTO virginia_dev_saayam_rdbms.notification_channels (channel_name, description) VALUES
    ('EMAIL',  'Outbound email delivered through Amazon SES'),
    ('SMS',    'Outbound text message delivered through Amazon SNS'),
    ('PUSH',   'Mobile push notification delivered through Amazon SNS'),
    ('IN_APP', 'In-app notification shown in the web/mobile notification centre')
ON CONFLICT (channel_name) DO NOTHING;

-- Type names match org.sfa.request.dto.notification.NotificationEventType.
INSERT INTO virginia_dev_saayam_rdbms.notification_types (type_name, description) VALUES
    ('REQUEST_CREATED',   'A new help request was created and matched to this volunteer'),
    ('REQUEST_UPDATED',   'A help request this user is involved in was updated'),
    ('REQUEST_CANCELLED', 'A help request this user is involved in was cancelled'),
    ('REQUEST_RESUMED',   'A previously cancelled help request was resumed'),
    ('REQUEST_DELETED',   'A help request this user is involved in was deleted'),
    ('VOLUNTEER_CHOSEN',  'This volunteer was chosen as lead or helping volunteer')
ON CONFLICT (type_name) DO NOTHING;
