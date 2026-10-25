--liquibase formatted sql
--changeset ayakovlev:008

UPDATE lessons SET start_time = '16:45' WHERE start_time = '16:55';