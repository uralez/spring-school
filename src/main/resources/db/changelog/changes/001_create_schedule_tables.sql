--liquibase formatted sql
--changeset ayakovlev:001

CREATE TABLE IF NOT EXISTS academic_calendar (
    calendar_date date PRIMARY KEY,
    day_of_week integer NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    day_type varchar(20) NOT NULL CHECK (day_type IN ('working','weekend','holiday','vacation','unknown'))
);

CREATE TABLE IF NOT EXISTS lessons (
    id serial PRIMARY KEY,
    day_of_week integer NOT NULL CHECK (day_of_week BETWEEN 1 AND 5),
    start_time time NOT NULL,
    classroom varchar(10) NOT NULL,
    subject_name varchar(100) NOT NULL,
    valid_from date NOT NULL,
    valid_to date
);

CREATE TABLE IF NOT EXISTS lesson_groups (
    lesson_id integer NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    class_name varchar(10) NOT NULL,
    PRIMARY KEY (lesson_id, class_name)
);