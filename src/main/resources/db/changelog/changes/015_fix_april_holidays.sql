--liquibase formatted sql
--changeset ayakovlev:015

UPDATE academic_calendar SET day_type = 'working'
WHERE calendar_date IN ('2027-04-16', '2027-04-19') AND day_of_week BETWEEN 1 AND 5;

UPDATE academic_calendar SET day_type = 'holiday'
WHERE calendar_date = '2027-04-30';