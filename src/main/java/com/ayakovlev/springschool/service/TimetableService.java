package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.entity.Lesson;
import com.ayakovlev.springschool.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final LessonRepository lessonRepository;

    @Transactional(readOnly = true)
    public Map<LocalTime, Map<Integer, List<Lesson>>> buildTimetable(LocalDate date) {
        Map<LocalTime, Map<Integer, List<Lesson>>> table = new TreeMap<>();
        for (Lesson lesson : lessonRepository.findActiveOn(date)) {
            table.computeIfAbsent(lesson.getStartTime(), t -> new TreeMap<>())
                    .computeIfAbsent(lesson.getDayOfWeek(), d -> new ArrayList<>())
                    .add(lesson);
        }
        return table;
    }
}