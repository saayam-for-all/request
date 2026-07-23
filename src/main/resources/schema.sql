-- ==========================================================
-- STEP 0: Create schema
-- ==========================================================
CREATE SCHEMA IF NOT EXISTS virginia_dev_saayam_rdbms;
SET search_path TO virginia_dev_saayam_rdbms;

-- ==========================================================
-- STEP 1: Sequences
-- ==========================================================
CREATE SEQUENCE IF NOT EXISTS virginia_dev_saayam_rdbms.user_id_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS virginia_dev_saayam_rdbms.request_id_seq START 1 INCREMENT 1;

-- ==========================================================
-- STEP 2: Reference Tables
-- ==========================================================
CREATE TABLE IF NOT EXISTS country (
    country_id SERIAL PRIMARY KEY,
    country_name VARCHAR(255) NOT NULL,
    phone_country_code INT NOT NULL,
    last_update_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS state (
    state_id SERIAL PRIMARY KEY,
    country_id INT NOT NULL,
    state_name VARCHAR(255) NOT NULL,
    last_update_date TIMESTAMP,
    UNIQUE (country_id, state_id),
    FOREIGN KEY (country_id) REFERENCES country (country_id)
);

CREATE TABLE IF NOT EXISTS user_status (
    user_status_id SERIAL PRIMARY KEY,
    user_status VARCHAR(255) NOT NULL,
    user_status_desc VARCHAR(255),
    last_update_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_category (
    user_category_id SERIAL PRIMARY KEY,
    user_category VARCHAR(255) NOT NULL,
    user_category_desc VARCHAR(255),
    last_update_date TIMESTAMP
);

-- ==========================================================
-- STEP 3: Users Table + Function + Trigger
-- ==========================================================
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(255) PRIMARY KEY,
    state_id INT NULL,
    country_id INT NULL,
    user_status_id INT NULL,
    user_category_id INT NULL,
    full_name VARCHAR(255),
    first_name VARCHAR(255),
    middle_name VARCHAR(255),
    last_name VARCHAR(255),
    primary_email_address VARCHAR(255),
    primary_phone_number VARCHAR(255),
    addr_ln1 VARCHAR(255),
    addr_ln2 VARCHAR(255),
    addr_ln3 VARCHAR(255),
    city_name VARCHAR(255),
    zip_code VARCHAR(255),
    last_location POINT,
    last_update_date TIMESTAMP,
    time_zone VARCHAR(255),
    profile_picture_path VARCHAR(255),
    gender VARCHAR(255),
    language_1 VARCHAR(255),
    language_2 VARCHAR(255),
    language_3 VARCHAR(255),
    promotion_wizard_stage INT,
    promotion_wizard_last_update_date TIMESTAMP,
    FOREIGN KEY (country_id) REFERENCES country (country_id),
    FOREIGN KEY (state_id) REFERENCES state (state_id),
    FOREIGN KEY (user_status_id) REFERENCES user_status (user_status_id),
    FOREIGN KEY (user_category_id) REFERENCES user_category (user_category_id)
);


CREATE OR REPLACE FUNCTION virginia_dev_saayam_rdbms.generate_sid()
RETURNS TRIGGER AS '
DECLARE
    seq_id INT;
    new_id VARCHAR(20);
BEGIN
    seq_id := nextval(''virginia_dev_saayam_rdbms.user_id_seq'');
    
    new_id := ''SID-00-'' || LPAD(FLOOR(seq_id / 1000000)::TEXT, 3, ''0'') || ''-'' ||
              LPAD(FLOOR((seq_id % 1000000) / 1000)::TEXT, 3, ''0'') || ''-'' ||
              LPAD((seq_id % 1000)::TEXT, 3, ''0'');
              
    NEW.user_id := new_id;
    RETURN NEW;
END;
' LANGUAGE plpgsql;




DO '
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = ''before_insert_users''
    ) THEN
        CREATE TRIGGER before_insert_users
        BEFORE INSERT ON users
        FOR EACH ROW
        EXECUTE FUNCTION generate_sid();
    END IF;
END;
' LANGUAGE plpgsql;


-- ==========================================================
-- STEP 4: Request Reference Tables
-- ==========================================================
CREATE TABLE IF NOT EXISTS request_status (
    req_status_id SERIAL PRIMARY KEY,
    req_status VARCHAR(255) NOT NULL,
    req_status_desc VARCHAR(255),
    last_updated_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS request_priority (
    req_priority_id SERIAL PRIMARY KEY,
    req_priority VARCHAR(255) NOT NULL,
    req_priority_desc VARCHAR(255),
    last_updated_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS request_type (
    req_type_id SERIAL PRIMARY KEY,
    req_type VARCHAR(255) NOT NULL,
    req_type_desc VARCHAR(255),
    last_updated_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS request_category (
    request_category_id SERIAL PRIMARY KEY,
    request_category VARCHAR(255) NOT NULL,
    request_category_desc VARCHAR(255),
    last_updated_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS help_categories (
    cat_id VARCHAR(50) PRIMARY KEY,
    cat_name VARCHAR(100) NOT NULL,
    cat_desc VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS request_isleadvol (
    req_islead_id SERIAL PRIMARY KEY,
    req_islead VARCHAR(15) NOT NULL,
    req_islead_desc VARCHAR(100),
    last_updated_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS request_for (
    req_for_id SERIAL PRIMARY KEY,
    req_for VARCHAR(255) NOT NULL,
    req_for_desc VARCHAR(255),
    last_updated_date TIMESTAMP
);

-- ==========================================================
-- STEP 5: Request Table + Sequence Function + Trigger
-- ==========================================================
CREATE TABLE IF NOT EXISTS request (
    req_id VARCHAR(255) PRIMARY KEY,
    req_user_id VARCHAR(255) NOT NULL,
    req_for_id INT NOT NULL,
    req_cat_id VARCHAR(50) NOT NULL,
    req_islead_id INT NOT NULL,
    req_type_id INT NOT NULL,
    req_priority_id INT NOT NULL,
    req_status_id INT NOT NULL,
    req_loc VARCHAR(125),
    req_subj VARCHAR(125) NOT NULL,
    req_desc VARCHAR(255) NOT NULL,
    audio_req_desc VARCHAR(255),
    submission_date TIMESTAMP,
    serviced_date TIMESTAMP,
    last_update_date TIMESTAMP,
    FOREIGN KEY (req_user_id) REFERENCES users (user_id),
    FOREIGN KEY (req_status_id) REFERENCES request_status (req_status_id),
    FOREIGN KEY (req_priority_id) REFERENCES request_priority (req_priority_id),
    FOREIGN KEY (req_type_id) REFERENCES request_type (req_type_id),
    FOREIGN KEY (req_cat_id) REFERENCES help_categories (cat_id),
    FOREIGN KEY (req_for_id) REFERENCES request_for (req_for_id),
    FOREIGN KEY (req_islead_id) REFERENCES request_isleadvol (req_islead_id)
);

CREATE OR REPLACE FUNCTION virginia_dev_saayam_rdbms.generate_request_id()
RETURNS TRIGGER AS '
DECLARE
    seq_id INT;
    new_id VARCHAR(30);
BEGIN
    seq_id := nextval(''virginia_dev_saayam_rdbms.request_id_seq'');
    new_id := ''REQ-'' || LPAD(FLOOR(seq_id / 100000000)::TEXT, 2, ''0'') || ''-'' ||
              LPAD(FLOOR((seq_id % 100000000) / 100000)::TEXT, 3, ''0'') || ''-'' ||
              LPAD(FLOOR((seq_id % 100000) / 1000)::TEXT, 3, ''0'') || ''-'' ||
              LPAD((seq_id % 1000)::TEXT, 4, ''0'');
    NEW.req_id := new_id;
    RETURN NEW;
END;
' LANGUAGE plpgsql;

DO '
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = ''before_insert_requests''
    ) THEN
        CREATE TRIGGER before_insert_requests
        BEFORE INSERT ON request
        FOR EACH ROW
        EXECUTE FUNCTION generate_request_id();
    END IF;
END;
' LANGUAGE plpgsql;

-- ==========================================================
-- STEP 6: Volunteer Assignments (issue #14)
-- One row per volunteer assigned to a request. volunteer_type is
-- 'LEAD' (at most one per request) or 'HELPING' (zero or more).
-- ==========================================================
CREATE TABLE IF NOT EXISTS volunteers_assigned (
    volunteers_assigned_id SERIAL PRIMARY KEY,
    request_id VARCHAR(255) NOT NULL,
    volunteer_id VARCHAR(255) NOT NULL,
    volunteer_type VARCHAR(255) NOT NULL,
    last_update_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_volunteers_assigned_request FOREIGN KEY (request_id) REFERENCES request (req_id),
    CONSTRAINT fk_volunteers_assigned_volunteer FOREIGN KEY (volunteer_id) REFERENCES users (user_id),
    CONSTRAINT chk_volunteer_type CHECK (volunteer_type IN ('LEAD', 'HELPING')),
    CONSTRAINT uq_volunteers_assigned_request_volunteer UNIQUE (request_id, volunteer_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_volunteers_assigned_lead
    ON volunteers_assigned (request_id)
    WHERE volunteer_type = 'LEAD';

CREATE INDEX IF NOT EXISTS idx_volunteers_assigned_request
    ON volunteers_assigned (request_id);
