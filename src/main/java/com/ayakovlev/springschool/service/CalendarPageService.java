package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.dto.CalendarCell;
import com.ayakovlev.springschool.dto.CalendarMonth;
import com.ayakovlev.springschool.entity.CalendarDay;
import com.ayakovlev.springschool.repository.CalendarDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalendarPageService {

    private final CalendarDayRepository calendarDayRepository;

    @Transactional(readOnly = true)
    public List<CalendarMonth> months(int year) {
        Map<LocalDate, String> types = new HashMap<>();
        for (CalendarDay d : calendarDayRepository
                .findByCalendarDateBetween(LocalDate.of(year, 9, 1), LocalDate.of(year + 1, 8, 31))) {
            types.put(d.getCalendarDate(), d.getDayType());
        }
        if (types.isEmpty()) {
            return List.of();
        }
        LocalDate first = Collections.min(types.keySet());
        LocalDate last = Collections.max(types.keySet());

        List<CalendarMonth> result = new ArrayList<>();
        for (YearMonth m = YearMonth.from(first); !m.isAfter(YearMonth.from(last)); m = m.plusMonths(1)) {
            List<List<CalendarCell>> weeks = new ArrayList<>();
            List<CalendarCell> week = new ArrayList<>();
            int offset = m.atDay(1).getDayOfWeek().getValue() - 1;
            for (int i = 0; i < offset; i++) {
                week.add(new CalendarCell(null, null));
            }
            for (int day = 1; day <= m.lengthOfMonth(); day++) {
                LocalDate date = m.atDay(day);
                String type;
                if (types.containsKey(date)) {
                    type = types.get(date);
                } else if (date.isBefore(first) || date.isAfter(last)) {
                    type = "out";
                } else {
                    type = "none";
                }
                week.add(new CalendarCell(date, type));
                if (week.size() == 7) {
                    weeks.add(week);
                    week = new ArrayList<>();
                }
            }
            if (!week.isEmpty()) {
                while (week.size() < 7) {
                    week.add(new CalendarCell(null, null));
                }
                weeks.add(week);
            }
            result.add(new CalendarMonth(m, weeks));
        }
        return result;
    }
}