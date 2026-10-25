--liquibase formatted sql
--changeset ayakovlev:006

INSERT INTO textbook (title, authors, publisher, publication_year, isbn, publisher_url)
VALUES ('Edexcel International GCSE (9-1) Physics Student Book',
        'Brian Arnold, Penny Johnson, Steve Woolley',
        'Pearson Education Limited', 2017, '978-0-435-18527-5',
        'https://www.pearsonglobalschools.com');

INSERT INTO subject (name, academic_year, language, textbook_id)
VALUES ('Physics', 2026, 'en', (SELECT id FROM textbook WHERE isbn = '978-0-435-18527-5'));

INSERT INTO subject_class (subject_id, class_name, grade)
SELECT id, '9', 9 FROM subject WHERE name = 'Physics' AND academic_year = 2026;