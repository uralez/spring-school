--liquibase formatted sql
--changeset ayakovlev:011

CREATE TABLE app_user (
    id bigserial PRIMARY KEY,
    dcre timestamp NOT NULL DEFAULT now(),
    dmod timestamp NOT NULL DEFAULT now(),
    user_cre varchar(100),
    user_mod varchar(100),
    username varchar(100) NOT NULL UNIQUE,
    password_hash varchar(100) NOT NULL
);

CREATE TABLE app_user_role (
    user_id bigint NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    role varchar(20) NOT NULL
        CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT', 'PARENT', 'DIRECTOR', 'DEPUTY', 'CURATOR', 'METHODIST')),
    PRIMARY KEY (user_id, role)
);