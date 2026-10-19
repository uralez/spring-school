package com.ayakovlev.springschool.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lessons")
@Getter
@Setter
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer dayOfWeek;
    private LocalTime startTime;
    private String classroom;
    private String subjectName;
    private LocalDate validFrom;
    private LocalDate validTo;

    @ElementCollection
    @CollectionTable(name = "lesson_groups", joinColumns = @JoinColumn(name = "lesson_id"))
    @Column(name = "class_name")
    private Set<String> groups = new HashSet<>();
}