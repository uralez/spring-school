--liquibase formatted sql
--changeset ayakovlev:018 splitStatements:false

ALTER TABLE lessons ADD COLUMN subject_id bigint REFERENCES subject (id);

DO $$
DECLARE
    r record;
    sid bigint;
    item text;
BEGIN
    CREATE TEMP TABLE lesson_keys ON COMMIT DROP AS
    SELECT l.id AS lesson_id, l.subject_name,
           (SELECT string_agg(g.class_name, ',' ORDER BY g.class_name)
            FROM lesson_groups g WHERE g.lesson_id = l.id) AS grp
    FROM lessons l;

    CREATE TEMP TABLE subject_keys ON COMMIT DROP AS
    SELECT s.id AS subject_id, s.name,
           (SELECT string_agg(c.class_name, ',' ORDER BY c.class_name)
            FROM subject_class c WHERE c.subject_id = s.id) AS grp
    FROM subject s WHERE s.academic_year = 2026;

    UPDATE lessons l SET subject_id = sk.subject_id
    FROM lesson_keys lk
    JOIN subject_keys sk ON sk.name = lk.subject_name AND sk.grp = lk.grp
    WHERE l.id = lk.lesson_id;

    FOR r IN SELECT DISTINCT lk.subject_name, lk.grp
             FROM lesson_keys lk JOIN lessons l ON l.id = lk.lesson_id
             WHERE l.subject_id IS NULL
    LOOP
        INSERT INTO subject (name, academic_year, language)
        VALUES (r.subject_name, 2026,
                CASE WHEN r.subject_name ~ '[а-яА-Я]' THEN 'ru' ELSE 'en' END)
        RETURNING id INTO sid;

        FOREACH item IN ARRAY string_to_array(r.grp, ',') LOOP
            INSERT INTO subject_class (subject_id, class_name, grade)
            VALUES (sid, item, split_part(item, '.', 1)::int);
        END LOOP;

        INSERT INTO subject_teacher (subject_id, teacher_id)
        SELECT sid, id FROM teacher ORDER BY id LIMIT 1;

        UPDATE lessons l SET subject_id = sid
        FROM lesson_keys lk
        WHERE l.id = lk.lesson_id AND lk.subject_name = r.subject_name AND lk.grp = r.grp;
    END LOOP;
END $$;