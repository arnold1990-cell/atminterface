INSERT INTO customers (id, full_name, card_number, pin_hash, status)
VALUES ('11111111-1111-1111-1111-111111111111', 'John Doe', '1234567890123456',
        '$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghijklmnopq', 'ACTIVE');

INSERT INTO accounts (id, customer_id, account_number, balance, currency, status)
VALUES ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111',
        '1002003001', 5000.00, 'USD', 'ACTIVE');
