package com.ayakovlev.springschool.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class SchoolClass {

    @Column(name = "class_name")
    private String className;

    private Integer grade;
}