CREATE TYPE user_origin AS ENUM ('form', 'google');

CREATE TABLE IF NOT EXISTS users
(
    id            bigserial PRIMARY KEY,
    provider_id   text                                        NOT NULL,
    username      text                                        NOT NULL,
    user_email    text                                        NOT NULL,
    user_password text,
    provider      user_origin NOT NULL
);

ALTER TABLE IF EXISTS users
    ADD CONSTRAINT unique_provider_id_provider UNIQUE (provider_id, provider);
ALTER TABLE IF EXISTS users
    ADD CONSTRAINT unique_user_email_provider UNIQUE (user_email, provider);

CREATE TABLE IF NOT EXISTS talks
(
    id          bigserial PRIMARY KEY,
    user_id     bigint NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name        text   NOT NULL,
    link        text   NOT NULL,
    description text,
    speaker     text,
    organizer   text,
    tags        text[] DEFAULT ARRAY [] ::text[]
);

CREATE TABLE IF NOT EXISTS favorites
(
    id      bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    talk_id bigint NOT NULL REFERENCES talks (id) ON DELETE CASCADE,
    UNIQUE (user_id, talk_id)
);
