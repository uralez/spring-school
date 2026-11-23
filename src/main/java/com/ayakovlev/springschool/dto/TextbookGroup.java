package com.ayakovlev.springschool.dto;

import com.ayakovlev.springschool.entity.Textbook;

import java.util.List;

public record TextbookGroup(String className, List<TextbookEntry> entries) {
}
