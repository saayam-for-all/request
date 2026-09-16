-- Issue #78: read-only verification of the deployed dev request statuses.
-- Run this against dev RDS before changing any status name or numeric ID.
BEGIN TRANSACTION READ ONLY;

SELECT *
FROM virginia_dev_saayam_rdbms.request_status
ORDER BY req_status_id;

SELECT
    req_status_id,
    COUNT(*) AS request_count
FROM virginia_dev_saayam_rdbms.request
GROUP BY req_status_id
ORDER BY req_status_id;

ROLLBACK;
