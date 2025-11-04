CREATE SEQUENCE janus_room_id_seq
    AS BIGINT
    START WITH 100000
    INCREMENT BY 1
    MINVALUE 100000
    NO MAXVALUE
    CACHE 100;

CREATE TABLE IF NOT EXISTS call_room
(
    id                         UUID        NOT NULL PRIMARY KEY,
    room_code                  VARCHAR(30) NOT NULL UNIQUE,
    host_id                    UUID,
    title                      VARCHAR(50) NOT NULL,
    max_participants           INT         NOT NULL,
    current_participants_count INT         NOT NULL,
    visibility                 VARCHAR(20) NOT NULL,
    tags                       VARCHAR(50)[],
    status                     VARCHAR(20) NOT NULL,
    join_type                  VARCHAR(20) NOT NULL,
    janus_room_id              BIGINT      NOT NULL UNIQUE,
    created_at                 TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_callroom_host_user FOREIGN KEY (host_id) REFERENCES users (id) ON DELETE SET NULL
);

ALTER SEQUENCE janus_room_id_seq OWNED BY call_room.janus_room_id;

CREATE TABLE call_conversation
(
    id              UUID        NOT NULL PRIMARY KEY,
    room_id         UUID        NOT NULL,
    sender_id       UUID,
    state           VARCHAR(20) NOT NULL,
    origin_text     TEXT        NOT NULL,
    origin_language VARCHAR(20) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_conversation_room_id FOREIGN KEY (room_id) REFERENCES call_room (id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE call_conversation_trans
(
    id                  UUID        NOT NULL PRIMARY KEY,
    room_id             UUID        NOT NULL,
    conversation_id     UUID        NOT NULL,
    translated_text     TEXT        NOT NULL,
    translated_language VARCHAR(20) NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_conversation_trans_room_id FOREIGN KEY (room_id) REFERENCES call_room (id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_trans_conversation_id FOREIGN KEY (conversation_id) REFERENCES call_conversation (id) ON DELETE CASCADE,
    CONSTRAINT uq_conversation_language UNIQUE (conversation_id, translated_language)
);

CREATE TABLE IF NOT EXISTS call_history
(
    id         UUID         NOT NULL PRIMARY KEY,
    room_id    UUID         NOT NULL,
    user_id    UUID         NOT NULL,
    title      VARCHAR(255) NOT NULL,
    summary    VARCHAR(1000),
    memo       VARCHAR(1000),
    liked      BOOLEAN                  DEFAULT FALSE,
    deleted    BOOLEAN                  DEFAULT FALSE,
    left_at    TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_room_id FOREIGN KEY (room_id) REFERENCES call_room (id) ON DELETE CASCADE,
    CONSTRAINT fk_history_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS call_participant
(
    id                UUID         NOT NULL PRIMARY KEY,
    room_id           UUID         NOT NULL,
    user_id           UUID,
    language          VARCHAR(20)  NOT NULL,
    country           VARCHAR(20)  NOT NULL,
    display_name      VARCHAR(100) NOT NULL,
    profile_image_url TEXT,
    left_at           TIMESTAMP,
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_participant_room_id FOREIGN KEY (room_id) REFERENCES call_room (id) ON DELETE CASCADE,
    CONSTRAINT fk_participant_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);