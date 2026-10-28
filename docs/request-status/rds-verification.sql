-- Issue #78: read-only verification of the deployed dev request statuses.
-- Run this against dev RDS before changing any status name or numeric ID.
BEGIN TRANSACTION READ ONLY;

SELECT *
FROM virginia_dev_saayam_rdbms.request_status
ORDER BY req_status_id;

SELECT
    r.req_status_id,
    s.req_status,
    COUNT(*) AS request_count
FROM virginia_dev_saayam_rdbms.request AS r
LEFT JOIN virginia_dev_saayam_rdbms.request_status AS s
    ON s.req_status_id = r.req_status_id
GROUP BY r.req_status_id, s.req_status
ORDER BY r.req_status_id;

-- Duplicate semantic names would make a name-based lookup ambiguous.
SELECT req_status, COUNT(*) AS row_count
FROM virginia_dev_saayam_rdbms.request_status
GROUP BY req_status
HAVING COUNT(*) > 1;

ROLLBACK;
