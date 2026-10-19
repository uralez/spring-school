--liquibase formatted sql
--changeset ayakovlev:003

CREATE TABLE academic_modules (
    module_number integer PRIMARY KEY CHECK (module_number > 0),
    start_date date NOT NULL,
    end_date date NOT NULL,
    CHECK (end_date >= start_date)
);

INSERT INTO academic_modules (module_number, start_date, end_date) VALUES
    (1, '2026-09-15', '2026-11-01'),
    (2, '2026-11-09', '2026-12-27'),
    (3, '2027-01-11', '2027-02-28'),
    (4, '2027-03-08', '2027-04-18'),
    (5, '2027-04-19', '2027-05-31');