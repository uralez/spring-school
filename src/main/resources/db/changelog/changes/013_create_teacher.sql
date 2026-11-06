--liquibase formatted sql
--changeset ayakovlev:013

CREATE TABLE teacher (
    id bigserial PRIMARY KEY,
    dcre timestamp NOT NULL DEFAULT now(),
    dmod timestamp NOT NULL DEFAULT now(),
    user_cre varchar(100),
    user_mod varchar(100),
    user_id bigint NOT NULL UNIQUE REFERENCES app_user (id)
);

CREATE TABLE subject_teacher (
    subject_id bigint NOT NULL REFERENCES subject (id) ON DELETE CASCADE,
    teacher_id bigint NOT NULL REFERENCES teacher (id) ON DELETE CASCADE,
    PRIMARY KEY (subject_id, teacher_id)
);

INSERT INTO teacher (user_id)
SELECT u.id FROM app_user u
JOIN app_user_role r ON r.user_id = u.id AND r.role = 'ADMIN'
ORDER BY u.id LIMIT 1;

INSERT INTO app_user_role (user_id, role)
SELECT t.user_id, 'TEACHER' FROM teacher t
ON CONFLICT DO NOTHING;

INSERT INTO subject_teacher (subject_id, teacher_id)
SELECT s.id, t.id FROM subject s CROSS JOIN teacher t;