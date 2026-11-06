package com.ayakovlev.springschool.controller;

import com.ayakovlev.springschool.service.AccessService;
import com.ayakovlev.springschool.service.CalendarSummaryService;
import com.ayakovlev.springschool.service.SubjectService;
import com.ayakovlev.springschool.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TimetableService timetableService;
    private final CalendarSummaryService calendarSummaryService;
    private final SubjectService subjectService;
    private final AccessService access;

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        LocalDate today = LocalDate.now();
        model.addAttribute("summary", calendarSummaryService.build(today));
        model.addAttribute("timetable", timetableService.buildTimetable(today));
        model.addAttribute("plannedKeys", subjectService.plannedKeys(today));
        model.addAttribute("canEditPlans", access.canEditPlans(authentication));
        return "index";
    }
}