package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.CalendarDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface CalendarDayRepository extends JpaRepository<CalendarDay, LocalDate> {

    @Query("SELECT COUNT(c) FROM CalendarDay c WHERE c.dayType = 'working' AND c.calendarDate <= :date")
    long countWorkingDaysUntil(@Param("date") LocalDate date);

    @Query("SELECT COUNT(c) FROM CalendarDay c WHERE c.dayType = 'working'")
    long countWorkingDays();

    @Query(value = "SELECT COUNT(DISTINCT date_trunc('week', calendar_date)) FROM academic_calendar WHERE day_type = 'working' AND calendar_date <= :date", nativeQuery = true)
    long countWorkingWeeksUntil(@Param("date") LocalDate date);

    @Query(value = "SELECT COUNT(DISTINCT date_trunc('week', calendar_date)) FROM academic_calendar WHERE day_type = 'working'", nativeQuery = true)
    long countWorkingWeeks();
}