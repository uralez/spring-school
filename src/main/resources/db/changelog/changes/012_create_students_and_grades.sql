--liquibase formatted sql
--changeset ayakovlev:012

ALTER TABLE app_user ADD COLUMN full_name varchar(150);
ALTER TABLE app_user ALTER COLUMN password_hash DROP NOT NULL;

CREATE TABLE school_class (
    id bigserial PRIMARY KEY,
    dcre timestamp NOT NULL DEFAULT now(),
    dmod timestamp NOT NULL DEFAULT now(),
    user_cre varchar(100),
    user_mod varchar(100),
    name varchar(10) NOT NULL,
    grade integer NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    UNIQUE (name, start_date)
);

CREATE TABLE study_group (
    id bigserial PRIMARY KEY,
    dcre timestamp NOT NULL DEFAULT now(),
    dmod timestamp NOT NULL DEFAULT now(),
    user_cre varchar(100),
    user_mod varchar(100),
    name varchar(10) NOT NULL,
    class_id bigint NOT NULL REFERENCES school_class (id),
    UNIQUE (name, class_id)
);

CREATE TABLE student (
    id bigserial PRIMARY KEY,
    dcre timestamp NOT NULL DEFAULT now(),
    dmod timestamp NOT NULL DEFAULT now(),
    user_cre varchar(100),
    user_mod varchar(100),
    user_id bigint NOT NULL UNIQUE REFERENCES app_user (id)
);

CREATE TABLE group_membership (
    id bigserial PRIMARY KEY,
    dcre timestamp NOT NULL DEFAULT now(),
    dmod timestamp NOT NULL DEFAULT now(),
    user_cre varchar(100),
    user_mod varchar(100),
    student_id bigint NOT NULL REFERENCES student (id) ON DELETE CASCADE,
    group_id bigint NOT NULL REFERENCES study_group (id),
    active boolean NOT NULL DEFAULT true,
    UNIQUE (student_id, group_id)
);

CREATE TABLE student_parent (
    student_id bigint NOT NULL REFERENCES student (id) ON DELETE CASCADE,
    user_id bigint NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    relation varchar(30),
    PRIMARY KEY (student_id, user_id)
);

CREATE TABLE student_grade (
    id bigserial PRIMARY KEY,
    dcre timestamp NOT NULL DEFAULT now(),
    dmod timestamp NOT NULL DEFAULT now(),
    user_cre varchar(100),
    user_mod varchar(100),
    student_id bigint NOT NULL REFERENCES student (id) ON DELETE CASCADE,
    course_lesson_id bigint NOT NULL REFERENCES course_lesson (id) ON DELETE CASCADE,
    score integer NOT NULL CHECK (score BETWEEN 0 AND 100),
    comment text
);

INSERT INTO school_class (name, grade, start_date, end_date) VALUES
    ('7/8', 7, '2026-09-15', '2027-05-31'),
    ('9', 9, '2026-09-15', '2027-05-31');

INSERT INTO study_group (name, class_id)
SELECT g.name, c.id
FROM (VALUES ('7', '7/8'), ('8.1', '7/8'), ('8.2', '7/8'), ('9', '9')) AS g(name, class_name)
JOIN school_class c ON c.name = g.class_name AND c.start_date = '2026-09-15';