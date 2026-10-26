package com.ayakovlev.springschool.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subject")
@Getter
@Setter
public class Subject extends BaseEntity {

    private String name;
    private Integer academicYear;
    private String language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "textbook_id")
    private Textbook textbook;

    @ElementCollection
    @CollectionTable(name = "subject_class", joinColumns = @JoinColumn(name = "subject_id"))
    private List<SchoolClass> classes = new ArrayList<>();
}