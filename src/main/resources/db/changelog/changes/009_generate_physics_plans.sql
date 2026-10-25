--liquibase formatted sql
--changeset ayakovlev:009

INSERT INTO subject (name, academic_year, language, textbook_id)
SELECT name, 2027, language, textbook_id FROM subject
WHERE name = 'Physics' AND academic_year = 2026;

INSERT INTO subject_class (subject_id, class_name, grade)
SELECT id, '10', 10 FROM subject WHERE name = 'Physics' AND academic_year = 2027;

INSERT INTO course_lesson (subject_id, lesson_number, lesson_type, topic, planned_date)
WITH slots AS (
    SELECT 1 AS yr, c.calendar_date AS d,
           row_number() OVER (ORDER BY c.calendar_date, l.start_time) AS n
    FROM academic_calendar c
    JOIN lessons l ON l.day_of_week = c.day_of_week
      AND c.calendar_date >= l.valid_from
      AND (l.valid_to IS NULL OR c.calendar_date <= l.valid_to)
    JOIN lesson_groups g ON g.lesson_id = l.id
    WHERE l.subject_name = 'Physics' AND g.class_name = '9' AND c.day_type = 'working'
    UNION ALL
    SELECT 2, g.d::date, row_number() OVER (ORDER BY g.d)
    FROM generate_series('2027-09-15'::date, '2028-05-31'::date, interval '1 day') AS g(d)
    LEFT JOIN academic_calendar c ON c.calendar_date = (g.d - interval '1 year')::date
    WHERE extract(isodow FROM g.d) IN (1, 3, 5)
      AND COALESCE(c.day_type, 'working') NOT IN ('holiday', 'vacation')
),
positioned AS (
    SELECT s.yr, s.d, s.n, m.module_number,
           row_number() OVER (PARTITION BY s.yr, m.module_number ORDER BY s.n DESC) AS k
    FROM slots s
    JOIN academic_modules m
      ON s.d BETWEEN m.start_date + ((s.yr - 1) * interval '1 year')
                 AND m.end_date + ((s.yr - 1) * interval '1 year')
),
typed AS (
    SELECT yr, d, n, module_number,
           CASE
             WHEN yr = 1 AND module_number = 5 AND k <= 3 THEN 'final'
             WHEN yr = 1 AND module_number = 5 AND k = 5 THEN 'test'
             WHEN yr = 1 AND module_number = 5 AND k = 6 THEN 'review'
             WHEN yr = 2 AND module_number = 5 AND k <= 2 THEN 'final'
             WHEN yr = 2 AND module_number = 5 AND k = 4 THEN 'test'
             WHEN yr = 2 AND module_number = 5 AND k IN (5, 6) THEN 'review'
             WHEN yr = 1 AND module_number < 5 AND k = 3 THEN 'test'
             WHEN yr = 1 AND module_number < 5 AND k = 4 THEN 'review'
             WHEN yr = 2 AND module_number < 5 AND k = 3 THEN 'test'
             WHEN yr = 2 AND module_number < 5 AND k IN (4, 5) THEN 'review'
             ELSE 'lesson'
           END AS kind
    FROM positioned
),
numbered AS (
    SELECT yr, d, n, module_number, kind,
           row_number() OVER (PARTITION BY yr, kind ORDER BY n) AS idx
    FROM typed
),
chapters (ch, title, cnt) AS (
    VALUES
    (1, 'Movement and position', 7),
    (2, 'Forces and shape', 5),
    (3, 'Forces and movement', 6),
    (4, 'Momentum', 4),
    (5, 'The turning effect of forces', 5),
    (6, 'Mains electricity', 4),
    (7, 'Current and voltage in circuits', 4),
    (8, 'Electrical resistance', 4),
    (9, 'Electric charge', 5),
    (10, 'Properties of waves', 4),
    (11, 'The electromagnetic spectrum', 3),
    (12, 'Light waves', 5),
    (13, 'Sound', 4),
    (14, 'Energy transfers', 3),
    (15, 'Thermal energy', 5),
    (16, 'Work and power', 4),
    (17, 'Energy resources and electricity generation', 6),
    (18, 'Density and pressure', 6),
    (19, 'Solids, liquids and gases', 11),
    (20, 'Magnetism and electromagnetism', 7),
    (21, 'Electric motors and electromagnetic induction', 11),
    (22, 'Atoms and radioactivity', 9),
    (23, 'Radiation and half-life', 6),
    (24, 'Applications of radioactivity', 7),
    (25, 'Fission and fusion', 6),
    (26, 'Motion in the universe', 5),
    (27, 'Stellar evolution', 4),
    (28, 'Cosmology', 4)
),
chapter_slots AS (
    SELECT CASE WHEN c.ch <= 17 THEN 1 ELSE 2 END AS yr,
           c.ch, c.title, c.cnt, g.part,
           row_number() OVER (PARTITION BY CASE WHEN c.ch <= 17 THEN 1 ELSE 2 END
                              ORDER BY c.ch, g.part) AS idx
    FROM chapters c
    CROSS JOIN LATERAL generate_series(1, c.cnt) AS g(part)
)
SELECT sub.id, t.n,
       CASE WHEN t.kind = 'final' THEN 'review' ELSE t.kind END,
       CASE t.kind
         WHEN 'test' THEN 'Test: module ' || t.module_number
         WHEN 'review' THEN 'Review: module ' || t.module_number
         WHEN 'final' THEN 'Final review'
         ELSE 'Chapter ' || cs.ch || '. ' || cs.title || ' (' || cs.part || ' of ' || cs.cnt || ')'
       END,
       t.d
FROM numbered t
JOIN subject sub ON sub.name = 'Physics' AND sub.academic_year = 2025 + t.yr
LEFT JOIN chapter_slots cs ON t.kind = 'lesson' AND cs.yr = t.yr AND cs.idx = t.idx
ORDER BY t.yr, t.n;