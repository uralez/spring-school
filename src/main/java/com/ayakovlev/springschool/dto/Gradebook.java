package com.ayakovlev.springschool.dto;

import com.ayakovlev.springschool.entity.CourseLesson;
import com.ayakovlev.springschool.entity.Student;
import com.ayakovlev.springschool.entity.Subject;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record Gradebook(Subject subject, List<CourseLesson> lessons, List<Student> students,
                        Map<String, String> cells, Set<Long> gradedLessons) {
}