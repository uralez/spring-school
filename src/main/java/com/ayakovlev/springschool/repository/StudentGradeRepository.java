package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.StudentGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface StudentGradeRepository extends JpaRepository<StudentGrade, Long> {

    List<StudentGrade> findByLessonIdOrderByDcre(Long lessonId);

    List<StudentGrade> findByLessonSubjectIdOrderByDcre(Long subjectId);

    boolean existsByLessonId(Long lessonId);

    @Query("SELECT DISTINCT g.lesson.id FROM StudentGrade g WHERE g.lesson.subject.id = :subjectId")
    Set<Long> gradedLessonIds(@Param("subjectId") Long subjectId);

    long countByLessonId(Long lessonId);
}