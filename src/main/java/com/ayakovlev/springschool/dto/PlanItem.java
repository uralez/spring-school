package com.ayakovlev.springschool.dto;

import com.ayakovlev.springschool.entity.CourseLesson;

import java.time.LocalDate;
import java.util.List;

public record PlanItem(String kind, Integer moduleNumber, LocalDate from, LocalDate to,
                       Integer weekNumber, List<CourseLesson> lessons) {

    public static PlanItem module(int number) {
        return new PlanItem("module", number, null, null, null, List.of());
    }

    public static PlanItem vacation(LocalDate from, LocalDate to) {
        return new PlanItem("vacation", null, from, to, null, List.of());
    }

    public static PlanItem week(int number, List<CourseLesson> lessons) {
        return new PlanItem("week", null, null, null, number, lessons);
    }
}