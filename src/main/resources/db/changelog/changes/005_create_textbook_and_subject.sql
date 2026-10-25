--liquibase formatted sql
--changeset ayakovlev:005

CREATE TABLE textbook (
    id bigserial PRIMARY KEY,
    dcre timestamp NOT NULL DEFAULT now(),
    dmod timestamp NOT NULL DEFAULT now(),
    user_cre varchar(100),
    user_mod varchar(100),
    title varchar(255) NOT NULL,
    authors varchar(255),
    publisher varchar(100),
    publication_year integer,
    isbn varchar(20),
    publisher_url text,
    file_url text
);

CREATE TABLE subject (
    id bigserial PRIMARY KEY,
    dcre timestamp NOT NULL DEFAULT now(),
    dmod timestamp NOT NULL DEFAULT now(),
    user_cre varchar(100),
    user_mod varchar(100),
    name varchar(100) NOT NULL,
    academic_year integer NOT NULL,
    language varchar(2) NOT NULL CHECK (language IN ('en', 'ru')),
    textbook_id bigint REFERENCES textbook (id)
);

CREATE TABLE subject_class (
    subject_id bigint NOT NULL REFERENCES subject (id) ON DELETE CASCADE,
    class_name varchar(10) NOT NULL,
    grade integer NOT NULL,
    PRIMARY KEY (subject_id, class_name)
);
