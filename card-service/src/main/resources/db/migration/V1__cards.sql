CREATE TABLE card
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    card_number VARCHAR(30)  NOT NULL UNIQUE,
    pin_hash    VARCHAR(255) NOT NULL,
    card_type   VARCHAR(20)  NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    customer_id BIGINT       NOT NULL,
    account_id  BIGINT       NOT NULL
);