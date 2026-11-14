--liquibase formatted sql
--changeset ayakovlev:019

ALTER TABLE course_lesson ADD COLUMN slot_id bigint REFERENCES lesson_slot (id);

DELETE FROM lesson_slot;

CREATE TEMP TABLE numbered ON COMMIT DROP AS
SELECT id, subject_id, planned_date,
       row_number() OVER (PARTITION BY subject_id ORDER BY planned_date, lesson_number, id) AS n
FROM course_lesson WHERE planned_date IS NOT NULL;

INSERT INTO lesson_slot (subject_id, slot_number, slot_date)
SELECT subject_id, n, planned_date FROM numbered;

UPDATE course_lesson c SET slot_id = s.id
FROM numbered n
JOIN lesson_slot s ON s.subject_id = n.subject_id AND s.slot_number = n.n
WHERE c.id = n.id;

ALTER TABLE course_lesson ADD CONSTRAINT uq_lesson_slot UNIQUE (slot_id);