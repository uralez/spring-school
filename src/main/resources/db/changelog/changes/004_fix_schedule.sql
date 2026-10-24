--liquibase formatted sql
--changeset ayakovlev:004

DELETE FROM lesson_groups
WHERE class_name = '8.2'
  AND lesson_id IN (SELECT id FROM lessons WHERE id = 5 AND day_of_week = 2 AND start_time = '14:00');

UPDATE lessons SET classroom = '23'
WHERE id = 9 AND day_of_week = 3 AND start_time = '12:10' AND classroom = '21';