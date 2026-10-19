package com.ayakovlev.springschool.dto;

import com.ayakovlev.springschool.entity.Lesson;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public record TimetableRow(LocalTime start, LocalTime end, boolean breakRow,
                           Map<Integer, List<Lesson>> lessons) {
}