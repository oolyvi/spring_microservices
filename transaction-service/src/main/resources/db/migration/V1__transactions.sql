CREATE TABLE transaction
(
    id                      BIGSERIAL PRIMARY KEY,
    idempotency_key         VARCHAR(100)   NOT NULL UNIQUE,
    type                    VARCHAR(20)    NOT NULL,
    status                  VARCHAR(20)    NOT NULL,
    amount                  NUMERIC(19, 2) NOT NULL,
    account_id              BIGINT         NOT NULL,
    related_account_id      BIGINT,
    original_transaction_id BIGINT,
    timestamp               TIMESTAMP      NOT NULL,
    CONSTRAINT chk_transaction_amount
        CHECK (amount > 0)
);

CREATE INDEX idx_transaction_account
    ON transaction (account_id);

CREATE INDEX idx_transaction_timestamp
    ON transaction (timestamp);