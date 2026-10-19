package com.ayakovlev.springschool.dto;

import java.time.LocalDate;

public record CalendarSummary(LocalDate date, String dayType,
                              long dayNumber, long totalDays,
                              long weekNumber, long totalWeeks,
                              Integer moduleNumber, long totalModules) {
}