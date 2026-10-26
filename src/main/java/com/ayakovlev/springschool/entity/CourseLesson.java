package com.ayakovlev.springschool.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "course_lesson")
@Getter
@Setter
public class CourseLesson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private Integer lessonNumber;
    private String lessonType;
    private String topic;
    private LocalDate plannedDate;
    private LocalDate actualDate;
    private String comment;
    private String lecture;
    private String classworkStart;
    private String classworkJoint;
    private String classworkEnd;
    private String homework;
}