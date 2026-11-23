package com.ayakovlev.springschool.dto;

import com.ayakovlev.springschool.entity.Textbook;

import java.time.LocalDate;
import java.util.List;

public record TextbookEntry(Textbook textbook, Long firstSubjectId, List<Usage> usage) {

    public record Usage(Long subjectId, String label, boolean coverageSet,
                        String approvedBy, LocalDate approvedOn) {
    }
}