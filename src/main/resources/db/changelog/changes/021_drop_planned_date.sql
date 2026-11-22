--liquibase formatted sql
--changeset ayakovlev:021

ALTER TABLE course_lesson DROP COLUMN planned_date;