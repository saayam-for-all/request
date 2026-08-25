-- Local demo schema. Column names and types mirror the `database` repo DDL so the
-- demo exercises the same mapping the deployed RDS instance would.
-- Scope is limited to the tables the notification path touches.

CREATE SCHEMA IF NOT EXISTS virginia_dev_saayam_rdbms;
SET search_path TO virginia_dev_saayam_rdbms;

DO $$ BEGIN
    CREATE TYPE status_type AS ENUM ('unread', 'read');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

CREATE TABLE country (
    country_id   SERIAL PRIMARY KEY,
    country_name VARCHAR(100) NOT NULL,
    phone_code   VARCHAR(5)   NOT NULL,
    country_code VARCHAR(6)   NOT NULL,
    is_eu_member BOOLEAN DEFAULT FALSE
);

CREATE TABLE user_status (
    user_status_id   SERIAL PRIMARY KEY,
    user_status      VARCHAR(255) NOT NULL,
    user_status_desc VARCHAR(255)
);

CREATE TABLE user_category (
    user_category_id   SERIAL PRIMARY KEY,
    user_category      VARCHAR(255) NOT NULL,
    user_category_desc VARCHAR(255)
);

CREATE TABLE users (
    user_id               VARCHAR(255) PRIMARY KEY,
    country_id            INT REFERENCES country (country_id),
    user_status_id        INT REFERENCES user_status (user_status_id),
    user_category_id      INT REFERENCES user_category (user_category_id),
    full_name             VARCHAR(255),
    first_name            VARCHAR(255),
    last_name             VARCHAR(255),
    primary_email_address VARCHAR(255),
    primary_phone_number  VARCHAR(255),
    city_name             VARCHAR(255),
    zip_code              VARCHAR(255),
    last_update_date      TIMESTAMP
);

CREATE TABLE help_categories (
    cat_id   VARCHAR(50)  PRIMARY KEY,
    cat_name VARCHAR(100) NOT NULL,
    cat_desc VARCHAR(150) NOT NULL
);

-- The join that makes volunteer matching possible: a volunteer's "skill" IS a
-- help-category id.
CREATE TABLE user_skills (
    user_id         VARCHAR(255) REFERENCES users (user_id),
    cat_id          VARCHAR(50)  NOT NULL REFERENCES help_categories (cat_id),
    created_at      TIMESTAMP DEFAULT (now() AT TIME ZONE 'UTC'),
    last_updated_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'UTC'),
    PRIMARY KEY (user_id, cat_id)
);

-- Presence of a row here is what makes a user a volunteer.
CREATE TABLE volunteer_details (
    user_id              VARCHAR(255) PRIMARY KEY REFERENCES users (user_id),
    terms_and_conditions BOOLEAN,
    availability_days    JSONB,
    availability_times   JSONB,
    created_at           TIMESTAMP DEFAULT (now() AT TIME ZONE 'UTC')
);

CREATE TABLE request_status   (req_status_id   SERIAL PRIMARY KEY, req_status   VARCHAR(25) NOT NULL, req_status_desc   VARCHAR(125), last_updated_date TIMESTAMP);
CREATE TABLE request_priority (req_priority_id SERIAL PRIMARY KEY, req_priority VARCHAR(25) NOT NULL, req_priority_desc VARCHAR(125), last_updated_date TIMESTAMP);
CREATE TABLE request_type     (req_type_id     SERIAL PRIMARY KEY, req_type     VARCHAR(25),          req_type_desc     VARCHAR(125), last_updated_date TIMESTAMP);
CREATE TABLE request_for      (req_for_id      SERIAL PRIMARY KEY, req_for      VARCHAR(25) NOT NULL, req_for_desc      VARCHAR(125), last_updated_date TIMESTAMP);
CREATE TABLE request_isleadvol(req_islead_id   SERIAL PRIMARY KEY, req_islead   VARCHAR(15) NOT NULL, req_islead_desc   VARCHAR(100), last_updated_date TIMESTAMP);

CREATE TABLE request (
    req_id          VARCHAR(255) PRIMARY KEY,
    req_user_id     VARCHAR(255) NOT NULL REFERENCES users (user_id),
    req_for_id      INT NOT NULL REFERENCES request_for (req_for_id),
    req_islead_id   INT NOT NULL REFERENCES request_isleadvol (req_islead_id),
    req_cat_id      VARCHAR(50) NOT NULL REFERENCES help_categories (cat_id),
    req_type_id     INT NOT NULL REFERENCES request_type (req_type_id),
    req_priority_id INT NOT NULL REFERENCES request_priority (req_priority_id),
    req_status_id   INT NOT NULL REFERENCES request_status (req_status_id),
    req_loc         VARCHAR(125),
    iscalamity      BOOLEAN,
    req_subj        VARCHAR(125) NOT NULL,
    req_desc        VARCHAR(255) NOT NULL,
    req_doc_link    TEXT,
    audio_req_desc  VARCHAR(255),
    submission_date TIMESTAMP,
    serviced_date   TIMESTAMP,
    last_update_date TIMESTAMP
);

-- Request-id generator, copied from ddl_request.sql.
CREATE SEQUENCE request_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

CREATE FUNCTION generate_request_id() RETURNS TRIGGER AS $$
DECLARE
    seq_id INT;
    new_id TEXT;
BEGIN
    seq_id := nextval('virginia_dev_saayam_rdbms.request_id_seq');
    new_id := 'REQ-' || LPAD(FLOOR(seq_id / 100000000)::TEXT, 2, '0') || '-' ||
              LPAD(FLOOR((seq_id % 100000000) / 100000)::TEXT, 3, '0') || '-' ||
              LPAD(FLOOR((seq_id % 100000) / 1000)::TEXT, 3, '0') || '-' ||
              LPAD((seq_id % 1000)::TEXT, 4, '0');
    NEW.req_id := new_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER before_insert_requests
BEFORE INSERT ON request
FOR EACH ROW EXECUTE FUNCTION generate_request_id();

CREATE TABLE notification_types (
    type_id     SERIAL PRIMARY KEY,
    type_name   VARCHAR(255) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE notification_channels (
    channel_id   SERIAL PRIMARY KEY,
    channel_name VARCHAR(255) UNIQUE NOT NULL,
    description  TEXT
);

CREATE TABLE notifications (
    notification_id  SERIAL PRIMARY KEY,
    user_id          VARCHAR(255) NOT NULL REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    type_id          INT NOT NULL REFERENCES notification_types (type_id),
    channel_id       INT NOT NULL REFERENCES notification_channels (channel_id),
    message          TEXT NOT NULL,
    status           status_type,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_update_date TIMESTAMP
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_status  ON notifications (status);

-- Watermark table read by the Volunteer service's notification APIs.
CREATE TABLE user_notification_status (
    user_id          VARCHAR(255) PRIMARY KEY REFERENCES users (user_id) ON DELETE CASCADE,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
