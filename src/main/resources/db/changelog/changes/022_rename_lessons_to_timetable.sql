--liquibase formatted sql
--changeset ayakovlev:022

ALTER TABLE lessons RENAME TO timetable;
ALTER SEQUENCE lessons_id_seq RENAME TO timetable_id_seq;