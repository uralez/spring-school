package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.dto.TimetableRow;
import com.ayakovlev.springschool.entity.TimetableEntry;
import com.ayakovlev.springschool.repository.TimetableEntryRepository;
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

    private final TimetableEntryRepository timetableEntryRepository;

    @Transactional(readOnly = true)
    public List<TimetableRow> buildTimetable(LocalDate date) {
        Map<LocalTime, Map<Integer, List<TimetableEntry>>> byTime = new TreeMap<>();
        for (TimetableEntry entry : timetableEntryRepository.findActiveOn(date)) {
            byTime.computeIfAbsent(entry.getStartTime(), t -> new TreeMap<>())
                    .computeIfAbsent(entry.getDayOfWeek(), d -> new ArrayList<>())
                    .add(entry);
        }

        List<TimetableRow> rows = new ArrayList<>();
        byTime.forEach((start, lessons) ->
                rows.add(new TimetableRow(start, start.plusMinutes(LESSON_MINUTES), false, lessons)));
        rows.add(new TimetableRow(BREAK_START, BREAK_END, true, Map.of()));
        rows.sort(Comparator.comparing(TimetableRow::start));
        return rows;
    }
}