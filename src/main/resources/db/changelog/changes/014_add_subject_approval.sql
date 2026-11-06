--liquibase formatted sql
--changeset ayakovlev:014

ALTER TABLE subject ADD COLUMN approved_by varchar(150);
ALTER TABLE subject ADD COLUMN approved_on date;