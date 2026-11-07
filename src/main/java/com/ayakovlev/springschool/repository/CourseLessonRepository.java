package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.CourseLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseLessonRepository extends JpaRepository<CourseLesson, Long> {

    List<CourseLesson> findBySubjectIdOrderByLessonNumber(Long subjectId);

    Optional<CourseLesson> findBySubjectIdAndLessonNumber(Long subjectId, Integer lessonNumber);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE course_lesson c SET lesson_number = r.n + 100000
        FROM (SELECT id, row_number() OVER (ORDER BY planned_date NULLS LAST, lesson_number, id) AS n
              FROM course_lesson WHERE subject_id = :subjectId) r
        WHERE c.id = r.id
        """, nativeQuery = true)
    void renumberStep1(@Param("subjectId") Long subjectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE course_lesson SET lesson_number = lesson_number - 100000 WHERE subject_id = :subjectId",
            nativeQuery = true)
    void renumberStep2(@Param("subjectId") Long subjectId);
}