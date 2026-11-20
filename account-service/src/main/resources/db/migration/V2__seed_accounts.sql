INSERT INTO account (account_number,
                     balance,
                     daily_limit,
                     account_type,
                     customer_id,
                     status)
VALUES ('AZ100000000000000001', 1250.00, 2000.00, 'CHECKING', 1, 'ACTIVE'),
       ('AZ100000000000000002', 340.00, 1000.00, 'SAVINGS', 1, 'ACTIVE'),
       ('AZ100000000000000003', 8500.50, 1000.50, 'CHECKING', 2, 'ACTIVE'),
       ('AZ100000000000000004', 8500.00, 1000.00, 'CHECKING', 3, 'ACTIVE') ON CONFLICT (account_number) DO NOTHING;