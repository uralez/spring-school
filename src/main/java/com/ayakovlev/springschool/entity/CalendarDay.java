package com.ayakovlev.springschool.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDate;

@Entity
@Table(name = "academic_calendar")
@Getter
public class CalendarDay {

    @Id
    private LocalDate calendarDate;
    private Integer dayOfWeek;
    private String dayType;
}