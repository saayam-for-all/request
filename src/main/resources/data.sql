-- Insert initial data into request_status table
INSERT INTO request_status (request_status_id, request_status, request_status_desc, last_updated_date)
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
ON CONFLICT (request_status_id) DO NOTHING;

-- Insert initial data into request_priority table
INSERT INTO request_priority (request_priority_id, request_priority, request_priority_desc, last_updated_date)
VALUES
    (0, 'UNSPECIFIED', 'Unspecified priority', now()),
    (1, 'LOW', 'Low priority', now()),
    (2, 'MEDIUM', 'Medium priority', now()),
    (3, 'HIGH', 'High priority', now()),
    (4, 'CRITICAL', 'Note: we will NOT handle any life threatening use cases', now())
ON CONFLICT (request_priority_id) DO NOTHING;

-- Insert initial data into request_type table
INSERT INTO request_type (request_type_id, request_type, request_type_desc, last_updated_date)
VALUES
    (0, 'UNSPECIFIED', 'Unspecified type', now()),
    (1, 'IN_PERSON', 'In-person request', now()),
    (2, 'REMOTE', 'Remote request', now())
ON CONFLICT (request_type_id) DO NOTHING;

-- Insert initial data into request_category table
INSERT INTO request_category (request_category_id, request_category, request_category_desc, last_updated_date)
VALUES
    (0, 'UNSPECIFIED', 'Unspecified category', now()),
    (1, 'TECHNICAL_SUPPORT', 'Technical support request', now()),
    (2, 'FINANCIAL_SUPPORT', 'Financial support request', now()),
    (3, 'LEGAL_SUPPORT', 'Legal support request', now()),
    (4, 'OTHER', 'Other types of support request', now())
ON CONFLICT (request_category_id) DO NOTHING;

-- Insert initial data into request_for table
INSERT INTO request_for (request_for_id, request_for, request_for_desc, last_updated_date)
VALUES
    (0, 'UNSPECIFIED', 'Unspecified request for', now()),
    (1, 'SELF', 'Request for self', now()),
    (2, 'OTHER', 'Request for others', now())
ON CONFLICT (request_for_id) DO NOTHING;
