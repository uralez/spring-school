package com.ayakovlev.springschool.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


/**
 * Один урок длинной 45 минут. У него есть тема, две самостоятельные, лекция, домашка и оценки.
 *
 * TODO Надо переименовать в Lesson, сейчас это сделать нельзя, потому что это имя занято классом, который мы переименуем в Timetable
 */
@Entity
@Table(name = "lesson")
@Getter
@Setter
public class Lesson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private Integer lessonNumber;
    private String lessonType;
    private String topic;
//    private LocalDate plannedDate;
    private LocalDate actualDate;
    private String comment;
    private String lecture;
    private String classworkStart;
    private String classworkJoint;
    private String classworkEnd;
    private String homework;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    private LessonSlot slot;

    public LocalDate getPlannedDate() {
        return slot != null ? slot.getSlotDate() : null;
    }
}