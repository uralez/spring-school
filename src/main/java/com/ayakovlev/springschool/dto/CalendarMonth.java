package com.ayakovlev.springschool.dto;

import java.time.YearMonth;
import java.util.List;

public record CalendarMonth(YearMonth month, List<List<CalendarCell>> weeks) {
}