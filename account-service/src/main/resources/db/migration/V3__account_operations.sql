CREATE TABLE account_operation
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idempotency_key VARCHAR(100)   NOT NULL UNIQUE,
    account_id      BIGINT         NOT NULL,
    type            VARCHAR(10)    NOT NULL CHECK (type IN ('DEBIT', 'CREDIT')),
    amount          NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_account_operation_account ON account_operation (account_id);
