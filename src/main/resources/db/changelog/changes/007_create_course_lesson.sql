--liquibase formatted sql
--changeset ayakovlev:007

CREATE TABLE course_lesson (
    id bigserial PRIMARY KEY,
    dcre timestamp NOT NULL DEFAULT now(),
    dmod timestamp NOT NULL DEFAULT now(),
    user_cre varchar(100),
    user_mod varchar(100),
    subject_id bigint NOT NULL REFERENCES subject (id) ON DELETE CASCADE,
    lesson_number integer NOT NULL CHECK (lesson_number > 0),
    lesson_type varchar(10) NOT NULL DEFAULT 'lesson' CHECK (lesson_type IN ('lesson', 'review', 'test')),
    topic text,
    planned_date date,
    actual_date date,
    comment text,
    lecture text,
    classwork_start text,
    classwork_joint text,
    classwork_end text,
    homework text,
    UNIQUE (subject_id, lesson_number)
);