SET search_path TO virginia_dev_saayam_rdbms;

-- Enum tables. IDs match org.sfa.request.model.enums.* (0-based).
INSERT INTO request_status (req_status_id, req_status) VALUES
    (0,'CREATED'),(1,'MATCHING_VOLUNTEER'),(2,'MANAGED'),(3,'RESOLVED'),(4,'CANCELLED'),(5,'DELETED');
INSERT INTO request_priority (req_priority_id, req_priority) VALUES
    (0,'LOW'),(1,'MEDIUM'),(2,'HIGH'),(3,'CRITICAL');
INSERT INTO request_type (req_type_id, req_type) VALUES (0,'INPERSON'),(1,'REMOTE');
INSERT INTO request_for  (req_for_id,  req_for)  VALUES (0,'SELF'),(1,'OTHER');
INSERT INTO request_isleadvol (req_islead_id, req_islead) VALUES (0,'NO'),(1,'YES');

INSERT INTO country (country_id, country_name, phone_code, country_code) VALUES
    (1,'United States','+1','US');
INSERT INTO user_status   (user_status_id, user_status)     VALUES (1,'ACTIVE'),(2,'INACTIVE');
INSERT INTO user_category (user_category_id, user_category) VALUES (1,'BENEFICIARY'),(2,'VOLUNTEER');

-- Help categories, shape per ddl_help_categories.sql comments.
INSERT INTO help_categories (cat_id, cat_name, cat_desc) VALUES
    ('0.0.0.0.0','GENERAL_CATEGORY','GENERAL_CATEGORY_DESC'),
    ('1','FOOD_AND_ESSENTIALS_SUPPORT','FOOD_AND_ESSENTIALS_SUPPORT_DESC'),
    ('1.1','FOOD_ASSISTANCE','FOOD_ASSISTANCE_DESC'),
    ('2','EDUCATION_SUPPORT','EDUCATION_SUPPORT_DESC'),
    ('2.1','COLLEGE_COUNSELLING','COLLEGE_COUNSELLING_DESC');
