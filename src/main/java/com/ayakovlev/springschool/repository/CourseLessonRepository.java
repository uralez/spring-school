package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.CourseLesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseLessonRepository extends JpaRepository<CourseLesson, Long> {

    List<CourseLesson> findBySubjectIdOrderByLessonNumber(Long subjectId);
}