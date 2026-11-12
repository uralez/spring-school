--liquibase formatted sql
--changeset ayakovlev:017

ALTER TABLE student_grade ADD COLUMN kind varchar(20)
    CHECK (kind IN ('classwork_start', 'classwork_end', 'homework', 'board', 'test', 'retake', 'other'));