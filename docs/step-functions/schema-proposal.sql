-- Proposal for saayam-for-all/database review only. DO NOT apply automatically.
-- The database repository owns migrations; this file is intentionally not loaded
-- by Spring (ddl-auto=none, sql.init.mode=never).

-- One stable submission key must own at most one request and execution. A real
-- migration must select the deployed schema and reconcile request ID types.
CREATE TABLE workflow_executions (
    submission_id VARCHAR(128) PRIMARY KEY,
    request_id VARCHAR(255) UNIQUE,
    execution_arn VARCHAR(512) UNIQUE,
    input_sha256 CHAR(64) NOT NULL,
    state VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Task tokens must be encrypted by the application/KMS before storage. The hash
-- is for lookup/deduplication; it cannot be used to call SendTaskSuccess.
CREATE TABLE workflow_callbacks (
    callback_id UUID PRIMARY KEY,
    request_id VARCHAR(255) NOT NULL,
    execution_arn VARCHAR(512) NOT NULL,
    purpose VARCHAR(48) NOT NULL,
    recipient_id VARCHAR(255) NOT NULL,
    token_sha256 CHAR(64) NOT NULL UNIQUE,
    token_ciphertext BYTEA NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX workflow_callbacks_pending_idx
    ON workflow_callbacks (request_id, purpose, expires_at)
    WHERE consumed_at IS NULL;

-- OTP challenges and rating records belong to their approved service/schema
-- design and are deliberately not specified as tables here.
