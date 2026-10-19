package com.ayakovlev.springschool.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDate;

@Entity
@Table(name = "academic_modules")
@Getter
public class AcademicModule {

    @Id
    private Integer moduleNumber;
    private LocalDate startDate;
    private LocalDate endDate;
}