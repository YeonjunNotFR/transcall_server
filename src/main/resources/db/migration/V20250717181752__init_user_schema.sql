CREATE TABLE IF NOT EXISTS users
(
    id                UUID         NOT NULL PRIMARY KEY,
    email             VARCHAR(255) NOT NULL UNIQUE,
    social_type       VARCHAR(50)  NOT NULL,
    nickname          VARCHAR(100) NOT NULL,
    language          VARCHAR(20)  NOT NULL,
    membership_plan   VARCHAR(50)  NOT NULL,
    profile_image_url TEXT,
    is_active         BOOLEAN   DEFAULT TRUE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refresh_token
(
    id         UUID      NOT NULL PRIMARY KEY,
    user_id    UUID      NOT NULL UNIQUE,
    token      TEXT      NOT NULL UNIQUE,
    expire_at  TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_quotas
(
    id                UUID      NOT NULL PRIMARY KEY,
    user_id           UUID      NOT NULL UNIQUE,
    remaining_seconds BIGINT    NOT NULL,
    reset_at          TIMESTAMP NOT NULL,
    CONSTRAINT fk_quota_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);