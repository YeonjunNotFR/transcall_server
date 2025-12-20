CREATE TABLE IF NOT EXISTS users
(
    id                UUID         NOT NULL PRIMARY KEY,
    email             VARCHAR(255) NOT NULL UNIQUE,
    social_type       VARCHAR(50)  NOT NULL,
    nickname          VARCHAR(100) NOT NULL,
    language          VARCHAR(20)  NOT NULL,
    country           VARCHAR(20)  NOT NULL,
    profile_image_url TEXT,
    is_active         BOOLEAN                  DEFAULT TRUE,
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_settings
(
    user_id             UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    is_push_enabled     BOOLEAN NOT NULL         DEFAULT TRUE,
    is_marketing_agreed BOOLEAN NOT NULL         DEFAULT FALSE,
    is_privacy_agreed   BOOLEAN NOT NULL         DEFAULT TRUE,
    is_terms_agreed     BOOLEAN NOT NULL         DEFAULT TRUE,
    privacy_version     VARCHAR(20),
    marketing_agreed_at TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_auth
(
    user_id         UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    social_id       VARCHAR(255) UNIQUE,
    refresh_token   TEXT UNIQUE,
    token_expire_at TIMESTAMP WITH TIME ZONE,
    last_login_at   TIMESTAMP WITH TIME ZONE,
    last_login_ip   VARCHAR(45),
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_quotas
(
    user_id           UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    remaining_seconds BIGINT                   NOT NULL DEFAULT 0,
    daily_ad_count    INT                               DEFAULT 0,
    last_ad_at        TIMESTAMP WITH TIME ZONE,
    reset_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE          DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE          DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quota_transactions
(
    id           UUID PRIMARY KEY,
    user_id      UUID        NOT NULL,
    amount       BIGINT      NOT NULL,
    type         VARCHAR(50) NOT NULL,
    reference_id VARCHAR(255),
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
