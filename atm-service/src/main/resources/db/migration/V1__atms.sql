CREATE TABLE atm
(
    id              BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(100) UNIQUE NOT NULL,
    type            VARCHAR(20)         NOT NULL,
    amount          NUMERIC(19, 2)      NOT NULL,
    account_id      BIGINT,
    timestamp       TIMESTAMP           NOT NULL
);