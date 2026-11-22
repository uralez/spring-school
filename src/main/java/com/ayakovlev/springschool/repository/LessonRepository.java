package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findBySubjectIdOrderByLessonNumber(Long subjectId);

    Optional<Lesson> findBySubjectIdAndLessonNumber(Long subjectId, Integer lessonNumber);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE lesson c SET lesson_number = r.n + 100000
        FROM (SELECT cl.id, row_number() OVER (ORDER BY s.slot_date NULLS LAST, cl.lesson_number, cl.id) AS n
              FROM lesson cl LEFT JOIN lesson_slot s ON s.id = cl.slot_id
              WHERE cl.subject_id = :subjectId) r
        WHERE c.id = r.id
        """, nativeQuery = true)
    void renumberStep1(@Param("subjectId") Long subjectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE lesson SET lesson_number = lesson_number - 100000 WHERE subject_id = :subjectId",
            nativeQuery = true)
    void renumberStep2(@Param("subjectId") Long subjectId);

    @Query("SELECT DISTINCT c.subject.id FROM Lesson c")
    Set<Long> findSubjectIdsWithLessons();
}