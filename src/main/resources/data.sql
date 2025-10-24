-- Sample data for Template Method pattern examples

MERGE INTO customers (customer_id, name, email, status) KEY (customer_id) VALUES
    ('CUST-001', 'Alice Johnson', 'alice.johnson@example.com', 'ACTIVE'),
    ('CUST-002', 'Bob Smith', 'bob.smith@example.com', 'ACTIVE'),
    ('CUST-003', 'Charlie Brown', 'charlie.brown@example.com', 'ACTIVE'),
    ('CUST-004', 'Diana Prince', 'diana.prince@example.com', 'INACTIVE'),
    ('CUST-005', 'Eve Wilson', 'eve.wilson@example.com', 'ACTIVE');
