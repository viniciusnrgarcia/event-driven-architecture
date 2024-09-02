CREATE SEQUENCE payment_id_seq start 1;

-- CREATE UNLOGGED TABLE IF NOT EXISTS customers (
    -- id INTEGER PRIMARY KEY DEFAULT nextval('payment_id_seq'),
CREATE TABLE IF NOT EXISTS payment (
    id SERIAL PRIMARY KEY NOT NULL,
    amount DECIMAL(20, 2) NOT NULL,
    customer_id INTEGER NOT NULL,
    transaction_id INTEGER,
    status INTEGER,
    status_description VARCHAR(50) NULL,
    uuid VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS payment_event (
    status INTEGER,
    uuid VARCHAR(100) NOT NULL,
    primary key (status, uuid)
);

CREATE TABLE IF NOT EXISTS payment_error (
    id INTEGER NULL,
    amount DECIMAL(20, 2) NULL,
    customer_id INTEGER NULL,
    transaction_id INTEGER null,
    status INTEGER null,
    status_description VARCHAR(50) NULL,
    uuid VARCHAR(100) NULL
);


-- CREATE UNLOGGED TABLE IF NOT EXISTS transactions_1 (
--CREATE TABLE IF NOT EXISTS payment_transaction (
--    id INTEGER NULL,
--    amount DECIMAL(20, 2) NOT NULL,
--    customer_id INTEGER NOT NULL,
--    transaction_id INTEGER,
--    status INTEGER,
--    status_description VARCHAR(50) NULL
--);
--CREATE INDEX idx_id_1 ON payment_transaction(id);
--CREATE INDEX idx_customer_id_1 ON payment_transaction(customer_id);

-- CREATE INDEX idx_customer_id ON transactions(id, created_at);
-- CREATE INDEX idx_cliente_realizada_em ON transacoes (cliente_id, realizada_em);

--INSERT INTO customers (id, limit_account, balance)
--VALUES
--    (1, -100000, 0),
--    (2, -80000, 0),
--    (3, -1000000, 0),
--    (4, -10000000, 0),
--    (5, -500000, 0);

-- id SERIAL PRIMARY KEY,


CREATE TABLE IF NOT EXISTS event_store (
    id varchar(100) null,
    created_at timestamp with time zone default current_timestamp,
    created_by varchar(100) null,
    json JSONB null
);


--CREATE TABLE json_data (
--    id SERIAL PRIMARY KEY,
--    data JSONB NOT NULL
--);



CREATE TABLE IF NOT EXISTS kafka_listeners_control (
    listener_id VARCHAR NULL,
    status INTEGER NULL
);

INSERT INTO kafka_listeners_control (listener_id, status) VALUES
    ('payment-service-id', 0),
    ('payment-constomer-id', 0),
    ('payment-batch-service-id', 0)
;