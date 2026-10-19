package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.dto.TimetableRow;
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

    private static final int LESSON_MINUTES = 45;
    private static final LocalTime BREAK_START = LocalTime.of(13, 0);
    private static final LocalTime BREAK_END = LocalTime.of(14, 0);

    private final LessonRepository lessonRepository;

    @Transactional(readOnly = true)
    public List<TimetableRow> buildTimetable(LocalDate date) {
        Map<LocalTime, Map<Integer, List<Lesson>>> byTime = new TreeMap<>();
        for (Lesson lesson : lessonRepository.findActiveOn(date)) {
            byTime.computeIfAbsent(lesson.getStartTime(), t -> new TreeMap<>())
                    .computeIfAbsent(lesson.getDayOfWeek(), d -> new ArrayList<>())
                    .add(lesson);
        }

        List<TimetableRow> rows = new ArrayList<>();
        byTime.forEach((start, lessons) ->
                rows.add(new TimetableRow(start, start.plusMinutes(LESSON_MINUTES), false, lessons)));
        rows.add(new TimetableRow(BREAK_START, BREAK_END, true, Map.of()));
        rows.sort(Comparator.comparing(TimetableRow::start));
        return rows;
    }
}