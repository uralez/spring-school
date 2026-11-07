package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.StudentGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface StudentGradeRepository extends JpaRepository<StudentGrade, Long> {

    List<StudentGrade> findByCourseLessonIdOrderByDcre(Long courseLessonId);

    List<StudentGrade> findByCourseLessonSubjectIdOrderByDcre(Long subjectId);

    boolean existsByCourseLessonId(Long courseLessonId);

    @Query("SELECT DISTINCT g.courseLesson.id FROM StudentGrade g WHERE g.courseLesson.subject.id = :subjectId")
    Set<Long> gradedLessonIds(@Param("subjectId") Long subjectId);

    long countByCourseLessonId(Long courseLessonId);
}