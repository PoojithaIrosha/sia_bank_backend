CREATE TABLE refresh_tokens
(
    refresh_token TEXT PRIMARY KEY,
    user_id       BIGINT    NOT NULL,
    expiry_date   TIMESTAMP NOT NULL,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);
