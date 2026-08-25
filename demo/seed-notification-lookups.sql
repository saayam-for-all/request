-- Applied by the demo runner. Mirrors db/notification_lookup_seed.sql, which is
-- the version to be vetted by the DBMS team for real environments.
SET search_path TO virginia_dev_saayam_rdbms;

INSERT INTO notification_channels (channel_name, description) VALUES
    ('EMAIL',  'Outbound email delivered through Amazon SES'),
    ('SMS',    'Outbound text message delivered through Amazon SNS'),
    ('PUSH',   'Mobile push notification delivered through Amazon SNS'),
    ('IN_APP', 'In-app notification shown in the web/mobile notification centre')
ON CONFLICT (channel_name) DO NOTHING;

INSERT INTO notification_types (type_name, description) VALUES
    ('REQUEST_CREATED',   'A new help request was created and matched to this volunteer'),
    ('REQUEST_UPDATED',   'A help request this user is involved in was updated'),
    ('REQUEST_CANCELLED', 'A help request this user is involved in was cancelled'),
    ('REQUEST_RESUMED',   'A previously cancelled help request was resumed'),
    ('REQUEST_DELETED',   'A help request this user is involved in was deleted'),
    ('VOLUNTEER_CHOSEN',  'This volunteer was chosen as lead or helping volunteer')
ON CONFLICT (type_name) DO NOTHING;
