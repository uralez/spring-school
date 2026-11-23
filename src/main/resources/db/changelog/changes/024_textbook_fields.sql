--liquibase formatted sql
--changeset ayakovlev:024

ALTER TABLE textbook ADD COLUMN years_designed integer CHECK (years_designed BETWEEN 1 AND 3);

ALTER TABLE subject ADD COLUMN coverage varchar(20)
    CHECK (coverage IN ('whole', 'first_half', 'second_half', 'two_thirds', 'other'));
ALTER TABLE subject ADD COLUMN coverage_note text;