package com.ayakovlev.springschool.controller;

import com.ayakovlev.springschool.service.CalendarSummaryService;
import com.ayakovlev.springschool.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TimetableService timetableService;
    private final CalendarSummaryService calendarSummaryService;

    @GetMapping("/")
    public String home(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("summary", calendarSummaryService.build(today));
        model.addAttribute("timetable", timetableService.buildTimetable(today));
        return "index";
    }
}