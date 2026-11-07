--liquibase formatted sql
--changeset ayakovlev:016

CREATE TABLE lesson_slot (
    id bigserial PRIMARY KEY,
    dcre timestamp NOT NULL DEFAULT now(),
    dmod timestamp NOT NULL DEFAULT now(),
    user_cre varchar(100),
    user_mod varchar(100),
    subject_id bigint NOT NULL REFERENCES subject (id) ON DELETE CASCADE,
    slot_number integer NOT NULL CHECK (slot_number > 0),
    slot_date date NOT NULL,
    UNIQUE (subject_id, slot_number)
);

INSERT INTO lesson_slot (subject_id, slot_number, slot_date)
SELECT subject_id,
       row_number() OVER (PARTITION BY subject_id ORDER BY planned_date, lesson_number),
       planned_date
FROM course_lesson
WHERE planned_date IS NOT NULL;