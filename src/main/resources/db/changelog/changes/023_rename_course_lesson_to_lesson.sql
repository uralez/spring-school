--liquibase formatted sql
--changeset ayakovlev:023

ALTER TABLE course_lesson RENAME TO lesson;
ALTER SEQUENCE course_lesson_id_seq RENAME TO lesson_id_seq;