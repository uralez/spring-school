package com.ayakovlev.springschool.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "textbook")
@Getter
@Setter
public class Textbook extends BaseEntity {

    private String title;
    private String authors;
    private String publisher;
    private Integer publicationYear;
    private String isbn;
    private String publisherUrl;
    private String fileUrl;
}