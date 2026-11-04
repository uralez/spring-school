package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.StudentGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentGradeRepository extends JpaRepository<StudentGrade, Long> {

    List<StudentGrade> findByCourseLessonIdOrderByDcre(Long courseLessonId);

    List<StudentGrade> findByCourseLessonSubjectIdOrderByDcre(Long subjectId);
}