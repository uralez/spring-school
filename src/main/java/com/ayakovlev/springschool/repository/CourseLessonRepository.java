package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.CourseLesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseLessonRepository extends JpaRepository<CourseLesson, Long> {

    List<CourseLesson> findBySubjectIdOrderByLessonNumber(Long subjectId);

    Optional<CourseLesson> findBySubjectIdAndLessonNumber(Long subjectId, Integer lessonNumber);
}