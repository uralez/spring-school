--liquibase formatted sql
--changeset ayakovlev:020

ALTER TABLE lesson_slot ADD COLUMN start_time time;
ALTER TABLE lesson_slot ADD COLUMN end_time time;
ALTER TABLE lesson_slot ADD COLUMN start_work_minutes integer NOT NULL DEFAULT 5;
ALTER TABLE lesson_slot ADD COLUMN end_work_minutes integer NOT NULL DEFAULT 5;

UPDATE lesson_slot s SET start_time = t.start_time, end_time = t.start_time + interval '45 minutes'
FROM (
    SELECT DISTINCT ON (s2.id) s2.id AS slot_id, l.start_time
    FROM lesson_slot s2
    JOIN academic_calendar c ON c.calendar_date = s2.slot_date
    JOIN lessons l ON l.subject_id = s2.subject_id
      AND l.day_of_week = c.day_of_week
      AND s2.slot_date >= l.valid_from
      AND (l.valid_to IS NULL OR s2.slot_date <= l.valid_to)
    ORDER BY s2.id, l.start_time
) t
WHERE s.id = t.slot_id;