--liquibase formatted sql
--changeset ayakovlev:025

ALTER TABLE textbook ADD COLUMN stored_file varchar(100);
ALTER TABLE textbook ADD COLUMN original_name varchar(255);
ALTER TABLE textbook ADD COLUMN preview_file varchar(100);