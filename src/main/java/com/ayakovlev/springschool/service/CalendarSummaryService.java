package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.dto.CalendarSummary;
import com.ayakovlev.springschool.entity.AcademicModule;
import com.ayakovlev.springschool.entity.CalendarDay;
import com.ayakovlev.springschool.repository.AcademicModuleRepository;
import com.ayakovlev.springschool.repository.CalendarDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CalendarSummaryService {

    private final CalendarDayRepository calendarDayRepository;
    private final AcademicModuleRepository academicModuleRepository;

    @Transactional(readOnly = true)
    public CalendarSummary build(LocalDate date) {
        String dayType = calendarDayRepository.findById(date)
                .map(CalendarDay::getDayType)
                .orElse("unknown");
        Integer moduleNumber = academicModuleRepository.findByDate(date)
                .map(AcademicModule::getModuleNumber)
                .orElse(null);
        return new CalendarSummary(
                date,
                dayType,
                calendarDayRepository.countWorkingDaysUntil(date),
                calendarDayRepository.countWorkingDays(),
                calendarDayRepository.countWorkingWeeksUntil(date),
                calendarDayRepository.countWorkingWeeks(),
                moduleNumber,
                academicModuleRepository.count()
        );
    }
}