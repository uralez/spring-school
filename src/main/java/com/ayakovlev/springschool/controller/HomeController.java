package com.ayakovlev.springschool.controller;

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

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("timetable", timetableService.buildTimetable(LocalDate.now()));
        return "index";
    }
}