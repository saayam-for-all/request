SET search_path TO virginia_dev_saayam_rdbms;

-- One beneficiary who files the request.
INSERT INTO users (user_id, country_id, user_status_id, user_category_id,
                   full_name, first_name, last_name, primary_email_address, primary_phone_number, city_name)
VALUES ('SID-00-000-000-001', 1, 1, 1,
        'Priya Beneficiary','Priya','Beneficiary','beneficiary@saayam.test','+15550000001','San Jose');

-- Four volunteers with different skill coverage, to prove matching actually filters.
INSERT INTO users (user_id, country_id, user_status_id, user_category_id,
                   full_name, first_name, last_name, primary_email_address, primary_phone_number, city_name)
VALUES
    ('SID-00-000-000-002', 1, 1, 2, 'Alex Foodbank','Alex','Foodbank','alex.volunteer@saayam.test','+15550000002','San Jose'),
    ('SID-00-000-000-003', 1, 1, 2, 'Blair Pantry','Blair','Pantry','blair.volunteer@saayam.test','+15550000003','Santa Clara'),
    ('SID-00-000-000-004', 1, 1, 2, 'Casey Tutor','Casey','Tutor','casey.volunteer@saayam.test','+15550000004','Fremont'),
    -- Deliberately has NO email: proves the in-app channel still reaches them.
    ('SID-00-000-000-005', 1, 1, 2, 'Dana NoEmail','Dana','NoEmail', NULL,'+15550000005','Milpitas');

INSERT INTO volunteer_details (user_id, terms_and_conditions) VALUES
    ('SID-00-000-000-002', TRUE),
    ('SID-00-000-000-003', TRUE),
    ('SID-00-000-000-004', TRUE),
    ('SID-00-000-000-005', TRUE);

-- Skills. Category '1.1' (FOOD_ASSISTANCE) is the one the demo request uses.
INSERT INTO user_skills (user_id, cat_id) VALUES
    ('SID-00-000-000-002','1.1'),      -- matches
    ('SID-00-000-000-003','1.1'),      -- matches
    ('SID-00-000-000-004','2.1'),      -- does NOT match (education)
    ('SID-00-000-000-005','1.1');      -- matches, but has no email

-- The beneficiary also happens to hold the matching skill. They must NOT be
-- notified about their own request.
INSERT INTO user_skills (user_id, cat_id) VALUES ('SID-00-000-000-001','1.1');
