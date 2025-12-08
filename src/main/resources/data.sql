-- Insert initial data into request_status table
INSERT INTO request_status (req_status_id, req_status, req_status_desc, last_updated_date)
VALUES
    (0, 'UNSPECIFIED', 'Unspecified status', now()),
    (1, 'CREATED', 'Request has been created', now()),
    (2, 'PENDING_VOLUNTEER_ASSIGNMENT', 'Pending volunteer assignment', now()),
    (3, 'IN_PROGRESS', 'Request is in progress', now()),
    (4, 'COMPLETED', 'Request has been completed', now()),
    (5, 'CANCELLED', 'Request has been cancelled', now()),
    (6, 'DELETED', 'Request has been deleted', now()),
    (7, 'RATED_BY_REQUESTER', 'Request has been rated by requester', now()),
    (8, 'RATED_BY_VOLUNTEER', 'Request has been rated by volunteer', now())
ON CONFLICT (req_status_id) DO NOTHING;

-- Insert initial data into request_priority table
INSERT INTO request_priority (req_priority_id, req_priority, req_priority_desc, last_updated_date)
VALUES
    (0, 'UNSPECIFIED', 'Unspecified priority', now()),
    (1, 'LOW', 'Low priority', now()),
    (2, 'MEDIUM', 'Medium priority', now()),
    (3, 'HIGH', 'High priority', now()),
    (4, 'CRITICAL', 'Note: we will NOT handle any life threatening use cases', now())
ON CONFLICT (req_priority_id) DO NOTHING;

-- Insert initial data into request_type table
INSERT INTO request_type (req_type_id, req_type, req_type_desc, last_updated_date)
VALUES
    (0, 'UNSPECIFIED', 'Unspecified type', now()),
    (1, 'IN_PERSON', 'In-person request', now()),
    (2, 'REMOTE', 'Remote request', now())
ON CONFLICT (req_type_id) DO NOTHING;


-- Insert initial data into help_categories table
INSERT INTO virginia_dev_saayam_rdbms.help_categories (cat_id, cat_name, cat_desc)
VALUES
('0.0.0.0.0', 'GENERAL_CATEGORY', 'GENERAL_CATEGORY_DESC'),
('1', 'FOOD_AND_ESSENTIALS_SUPPORT', 'FOOD_AND_ESSENTIALS_SUPPORT_DESC'),
('1.1', 'FOOD_ASSISTANCE', 'FOOD_ASSISTANCE_DESC'),
('2', 'CLOTHING_SUPPORT', 'CLOTHING_SUPPORT_DESC')
ON CONFLICT (cat_id) DO NOTHING;

-- Insert initial data into request_isleadvol table
INSERT INTO virginia_dev_saayam_rdbms.request_isleadvol
(req_islead_id, req_islead, req_islead_desc, last_updated_date)
VALUES
(0, 'NO', 'Contributes to the request under the direction of the lead', NOW()),
(1, 'YES', 'The main point of contact with full authority over the request process', NOW())
ON CONFLICT (req_islead_id) DO NOTHING;


-- Insert initial data into request_category table
INSERT INTO virginia_dev_saayam_rdbms.request_category (request_category_id, request_category, request_category_desc, last_updated_date)
VALUES
    (0, 'UNSPECIFIED', 'Unspecified category', now()),
    (1, 'TECHNICAL_SUPPORT', 'Technical support request', now()),
    (2, 'FINANCIAL_SUPPORT', 'Financial support request', now()),
    (3, 'LEGAL_SUPPORT', 'Legal support request', now()),
    (4, 'OTHER', 'Other types of support request', now())
ON CONFLICT (request_category_id) DO NOTHING;

-- Insert initial data into request_for table
INSERT INTO request_for (req_for_id, req_for, req_for_desc, last_updated_date)
VALUES
    (0, 'UNSPECIFIED', 'Unspecified request for', now()),
    (1, 'SELF', 'Request for self', now()),
    (2, 'OTHER', 'Request for others', now())
ON CONFLICT (req_for_id) DO NOTHING;


-- Country
INSERT INTO virginia_dev_saayam_rdbms.country (country_name, phone_country_code, last_update_date)
VALUES ('USA', 1, NOW())
ON CONFLICT (country_id) DO NOTHING;

-- State
INSERT INTO virginia_dev_saayam_rdbms.state (country_id, state_name, last_update_date)
VALUES (1, 'California', NOW())
ON CONFLICT (state_id) DO NOTHING;

-- User Status
INSERT INTO virginia_dev_saayam_rdbms.user_status (user_status, user_status_desc, last_update_date)
VALUES 
('ACTIVE', 'Active user', NOW()),
('INACTIVE', 'Inactive user', NOW())
ON CONFLICT (user_status_id) DO NOTHING;

-- User Category
INSERT INTO virginia_dev_saayam_rdbms.user_category (user_category, user_category_desc, last_update_date)
VALUES 
('GENERAL', 'General user', NOW()),
('PREMIUM', 'Premium user', NOW())
ON CONFLICT (user_category_id) DO NOTHING;

INSERT INTO virginia_dev_saayam_rdbms.users (
    state_id,
    country_id,
    user_status_id,
    user_category_id,
    full_name,
    first_name,
    middle_name,
    last_name,
    primary_email_address,
    primary_phone_number,
    addr_ln1,
    addr_ln2,
    addr_ln3,
    city_name,
    zip_code,
    last_location,
    last_update_date,
    time_zone,
    profile_picture_path,
    gender,
    language_1,
    language_2,
    language_3,
    promotion_wizard_stage,
    promotion_wizard_last_update_date
)
VALUES
(1, 1, 1, 1, 'John Doe', 'John', NULL, 'Doe', 'john.doe@example.com', '1234567890', '123 Main St', NULL, NULL, 'San Jose', '95112', POINT(37.3382, -121.8863), NOW(), 'PST', NULL, 'Male', 'English', NULL, NULL, 1, NOW()),
(1, 1, 1, 2, 'Jane Smith', 'Jane', 'A', 'Smith', 'jane.smith@example.com', '0987654321', '456 Elm St', NULL, NULL, 'Los Angeles', '90001', POINT(34.0522, -118.2437), NOW(), 'PST', NULL, 'Female', 'English', 'Spanish', NULL, 2, NOW()),
(1, 1, 2, 1, 'Alice Johnson', 'Alice', NULL, 'Johnson', 'alice.johnson@example.com', '5551234567', '789 Oak St', 'Apt 101', NULL, 'San Francisco', '94103', POINT(37.7749, -122.4194), NOW(), 'PST', NULL, 'Female', 'English', NULL, NULL, 1, NOW());

