package com.ayakovlev.springschool.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "school_class")
@Getter
@Setter
public class ClassYear extends BaseEntity {

    private String name;
    private Integer grade;
    private LocalDate startDate;
    private LocalDate endDate;
}