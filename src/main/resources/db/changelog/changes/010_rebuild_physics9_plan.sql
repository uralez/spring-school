--liquibase formatted sql
--changeset ayakovlev:010

UPDATE course_lesson
SET lesson_type = CASE lesson_number WHEN 15 THEN 'review' WHEN 16 THEN 'test' ELSE 'lesson' END,
    topic = CASE lesson_number WHEN 15 THEN 'Review: module 1' WHEN 16 THEN 'Test: module 1' ELSE topic END
WHERE lesson_number IN (15, 16, 17)
  AND subject_id = (SELECT id FROM subject WHERE name = 'Physics' AND academic_year = 2026);

WITH chapters (ch, title, cnt) AS (
    VALUES
    (1, 'Movement and position', 4),
    (2, 'Forces and shape', 3),
    (3, 'Forces and movement', 4),
    (4, 'Momentum', 3),
    (5, 'The turning effect of forces', 5),
    (6, 'Mains electricity', 4),
    (7, 'Current and voltage in circuits', 4),
    (8, 'Electrical resistance', 5),
    (9, 'Electric charge', 6),
    (10, 'Properties of waves', 5),
    (11, 'The electromagnetic spectrum', 4),
    (12, 'Light waves', 6),
    (13, 'Sound', 5),
    (14, 'Energy transfers', 3),
    (15, 'Thermal energy', 6),
    (16, 'Work and power', 4),
    (17, 'Energy resources and electricity generation', 7)
),
chapter_slots AS (
    SELECT c.ch, c.title, c.cnt, g.part,
           row_number() OVER (ORDER BY c.ch, g.part) AS idx
    FROM chapters c
    CROSS JOIN LATERAL generate_series(1, c.cnt) AS g(part)
),
slots AS (
    SELECT cl.id, row_number() OVER (ORDER BY cl.lesson_number) AS idx
    FROM course_lesson cl
    WHERE cl.subject_id = (SELECT id FROM subject WHERE name = 'Physics' AND academic_year = 2026)
      AND cl.lesson_type = 'lesson'
      AND (cl.lesson_number <= 14 OR cl.lesson_number >= 17)
)
UPDATE course_lesson cl
SET topic = 'Chapter ' || cs.ch || '. ' || cs.title || ' (' || cs.part || ' of ' || cs.cnt || ')'
FROM slots sl
JOIN chapter_slots cs ON cs.idx = sl.idx
WHERE cl.id = sl.id;

UPDATE course_lesson
SET actual_date = planned_date
WHERE lesson_number <= 17
  AND subject_id = (SELECT id FROM subject WHERE name = 'Physics' AND academic_year = 2026);