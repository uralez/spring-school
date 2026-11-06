package com.ayakovlev.springschool.dto;

import java.time.LocalDate;

public record CalendarCell(LocalDate date, String type) {
}