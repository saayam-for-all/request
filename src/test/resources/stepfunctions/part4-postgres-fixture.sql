-- Isolated test fixture for the fields read by the JPA mappings. Never applied by the application.
CREATE SCHEMA virginia_dev_saayam_rdbms;
SET search_path TO virginia_dev_saayam_rdbms;
CREATE TABLE request_status (
    req_status_id integer PRIMARY KEY,
    req_status varchar(25) NOT NULL,
    req_status_desc varchar(125),
    last_updated_date timestamp
);
CREATE TABLE help_categories (cat_id varchar(255) PRIMARY KEY, cat_name varchar(255), cat_desc varchar(2000));
CREATE TABLE request_priority (req_priority_id integer PRIMARY KEY, req_priority varchar(255),
                               req_priority_desc varchar(255), last_updated_date timestamp);
CREATE TABLE request_type (req_type_id integer PRIMARY KEY, req_type varchar(255),
                           req_type_desc varchar(255), last_updated_date timestamp);
CREATE TABLE request_for (req_for_id integer PRIMARY KEY, req_for varchar(255),
                          req_for_desc varchar(255), last_updated_date timestamp);
CREATE TABLE request_isleadvol (req_islead_id integer PRIMARY KEY, req_islead varchar(255),
                                req_islead_desc varchar(255), last_updated_date timestamp);
CREATE TABLE request (
    req_id varchar(255) PRIMARY KEY,
    req_user_id varchar(255) NOT NULL,
    req_status_id integer REFERENCES request_status,
    req_priority_id integer NOT NULL REFERENCES request_priority,
    req_type_id integer NOT NULL REFERENCES request_type,
    req_cat_id varchar(255) NOT NULL REFERENCES help_categories,
    req_for_id integer NOT NULL REFERENCES request_for,
    req_loc varchar(125),
    iscalamity boolean,
    req_subj varchar(125) NOT NULL,
    req_desc varchar(255) NOT NULL,
    req_doc_link text,
    audio_req_desc varchar(255),
    req_islead_id integer NOT NULL REFERENCES request_isleadvol,
    submission_date timestamp,
    serviced_date timestamp,
    last_update_date timestamp
);
CREATE TABLE volunteers_assigned (
    volunteers_assigned_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id varchar(255) REFERENCES request,
    volunteer_id varchar(255) NOT NULL,
    volunteer_type varchar(255) NOT NULL,
    last_update_date timestamp NOT NULL
);
CREATE TABLE part4_test_applied_decision (id varchar(255) PRIMARY KEY);
INSERT INTO request_status (req_status_id, req_status) VALUES (61, 'IN_PROGRESS'), (87, 'RESOLVED');
INSERT INTO help_categories (cat_id, cat_name) VALUES ('cat', 'Tutoring');
INSERT INTO request_priority (req_priority_id, req_priority) VALUES (1, 'NORMAL');
INSERT INTO request_type (req_type_id, req_type) VALUES (1, 'REMOTE');
INSERT INTO request_for (req_for_id, req_for) VALUES (1, 'SELF');
INSERT INTO request_isleadvol (req_islead_id, req_islead) VALUES (1, 'NO');
INSERT INTO request (req_id, req_user_id, req_status_id, req_priority_id, req_type_id, req_cat_id,
                     req_for_id, req_subj, req_desc, req_islead_id)
VALUES ('req', 'beneficiary', 61, 1, 1, 'cat', 1, 'Test request', 'Test description', 1);
INSERT INTO volunteers_assigned (request_id, volunteer_id, volunteer_type, last_update_date)
VALUES ('req', 'lead', 'LEAD', now());
RESET search_path;
