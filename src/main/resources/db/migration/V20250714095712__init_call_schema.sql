CREATE TABLE call_room
(
    room_code        UUID PRIMARY KEY,
    host_public_id   UUID,
    title            VARCHAR(255) NOT NULL,
    max_participants INT          NOT NULL,
    is_locked        BOOLEAN      NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_callroom_host_user FOREIGN KEY (host_public_id) REFERENCES users (public_id) ON DELETE SET NULL
);

CREATE TABLE call_participant
(
    id                UUID PRIMARY KEY,
    room_code         UUID         NOT NULL,
    user_public_id    UUID,
    language          VARCHAR(20)  NOT NULL,
    display_name      VARCHAR(100) NOT NULL,
    profile_image_url TEXT,
    history_title     VARCHAR(255),
    history_summary   TEXT,
    history_memo      TEXT,
    history_liked     BOOLEAN   DEFAULT FALSE,
    history_deleted   BOOLEAN   DEFAULT FALSE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_participant_room_code FOREIGN KEY (room_code) REFERENCES call_room (room_code) ON DELETE CASCADE,
    CONSTRAINT fk_participant_user FOREIGN KEY (user_public_id) REFERENCES users (public_id) ON DELETE SET NULL
);

CREATE TABLE call_conversation
(
    id              UUID PRIMARY KEY,
    room_code       UUID        NOT NULL,
    sender_id       UUID,
    origin_text     TEXT        NOT NULL,
    origin_language VARCHAR(20) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_conversation_room_code FOREIGN KEY (room_code) REFERENCES call_room (room_code) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_sender FOREIGN KEY (sender_id) REFERENCES users (public_id) ON DELETE SET NULL
);

CREATE TABLE call_conversation_trans
(
    id                  UUID PRIMARY KEY,
    room_code           UUID        NOT NULL,
    conversation_id     UUID        NOT NULL,
    receiver_id         UUID,
    translated_text     TEXT        NOT NULL,
    translated_language VARCHAR(20) NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_conversation_trans_room_code FOREIGN KEY (room_code) REFERENCES call_room (room_code) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_trans_conversation_id FOREIGN KEY (conversation_id) REFERENCES call_conversation (id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_receiver_id FOREIGN KEY (receiver_id) REFERENCES users (public_id) ON DELETE SET NULL
);

CREATE TABLE call_history
(
    id               UUID PRIMARY KEY,
    room_code        UUID      NOT NULL,
    started_at       TIMESTAMP NOT NULL,
    ended_at         TIMESTAMP NOT NULL,
    duration_seconds INT       NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_room_code FOREIGN KEY (room_code) REFERENCES call_room (room_code) ON DELETE CASCADE
);