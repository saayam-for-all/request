-- Create request_id_seq sequence
CREATE SEQUENCE IF NOT EXISTS request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Create request_status table
CREATE TABLE IF NOT EXISTS request_status (
                                              request_status_id SERIAL PRIMARY KEY,
                                              request_status VARCHAR(255) NOT NULL,
                                              request_status_desc VARCHAR(255),
                                              last_updated_date TIMESTAMP,
                                              CONSTRAINT request_status_id_unique UNIQUE (request_status_id)
);

-- Create request_type table
CREATE TABLE IF NOT EXISTS request_type (
                                            request_type_id SERIAL PRIMARY KEY,
                                            request_type VARCHAR(255) NOT NULL,
                                            request_type_desc VARCHAR(255),
                                            last_updated_date TIMESTAMP,
                                            CONSTRAINT request_type_id_unique UNIQUE (request_type_id)
);

-- Create request_category table
CREATE TABLE IF NOT EXISTS request_category (
                                                request_category_id SERIAL PRIMARY KEY,
                                                request_category VARCHAR(255) NOT NULL,
                                                request_category_desc VARCHAR(255),
                                                last_updated_date TIMESTAMP,
                                                CONSTRAINT request_category_id_unique UNIQUE (request_category_id)
);

-- Create request_priority table
CREATE TABLE IF NOT EXISTS request_priority (
                                                request_priority_id SERIAL PRIMARY KEY,
                                                request_priority VARCHAR(255) NOT NULL,
                                                request_priority_desc VARCHAR(255),
                                                last_updated_date TIMESTAMP,
                                                CONSTRAINT request_priority_id_unique UNIQUE (request_priority_id)
);

-- Create request_for table
CREATE TABLE IF NOT EXISTS request_for (
                                           request_for_id SERIAL PRIMARY KEY,
                                           request_for VARCHAR(255) NOT NULL,
                                           request_for_desc VARCHAR(255),
                                           last_updated_date TIMESTAMP,
                                           CONSTRAINT request_for_id_unique UNIQUE (request_for_id)
);

-- Create request table
CREATE TABLE IF NOT EXISTS request (
                                       request_id VARCHAR(255) PRIMARY KEY,
                                       request_user_id VARCHAR(255) NOT NULL,
                                       request_status_id INT NOT NULL,
                                       request_priority_id INT NOT NULL,
                                       request_type_id INT NOT NULL,
                                       request_category_id INT NOT NULL,
                                       request_for_id INT NOT NULL,
                                       city_name VARCHAR(255),
                                       zip_code VARCHAR(20),
                                       request_desc VARCHAR(255) NOT NULL,
                                       audio_req_desc VARCHAR(255) NULL,
                                       submission_date TIMESTAMP,
                                       lead_volunteer_user_id INT,
                                       serviced_date TIMESTAMP,
                                       last_update_date TIMESTAMP,
                                       CONSTRAINT request_id_unique UNIQUE (request_id),
                                       CONSTRAINT fk_request_status_id FOREIGN KEY (request_status_id) REFERENCES request_status(request_status_id),
                                       CONSTRAINT fk_request_priority_id FOREIGN KEY (request_priority_id) REFERENCES request_priority (request_priority_id),
                                       CONSTRAINT fk_request_type_id FOREIGN KEY (request_type_id) REFERENCES request_type(request_type_id),
                                       CONSTRAINT fk_request_category_id FOREIGN KEY (request_category_id) REFERENCES request_category (request_category_id),
                                       CONSTRAINT fk_request_for_id FOREIGN KEY (request_for_id) REFERENCES request_for (request_for_id)
);

-- Create the function to generate the formatted request ID
CREATE OR REPLACE FUNCTION generate_request_id()
    RETURNS TRIGGER AS '
    DECLARE
        seq_id INT;
        new_id VARCHAR(30);
    BEGIN
        seq_id := nextval(''request_id_seq'');
        new_id := ''REQ-'' || LPAD(FLOOR(seq_id / 100000000)::TEXT, 2, ''0'') || ''-'' ||
                  LPAD(FLOOR((seq_id % 100000000) / 100000)::TEXT, 3, ''0'') || ''-'' ||
                  LPAD(FLOOR((seq_id % 100000) / 1000)::TEXT, 3, ''0'') || ''-'' ||
                  LPAD((seq_id % 1000)::TEXT, 4, ''0'');
        NEW.request_id := new_id;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

-- Create the trigger to use the function
DO '
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = ''before_insert_requests'') THEN
        CREATE TRIGGER before_insert_requests
            BEFORE INSERT ON request
            FOR EACH ROW
        EXECUTE FUNCTION generate_request_id();
    END IF;
END;
'
