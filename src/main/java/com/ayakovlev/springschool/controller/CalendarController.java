package com.ayakovlev.springschool.controller;

import com.ayakovlev.springschool.service.CalendarPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarPageService calendarPageService;

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) Integer year, Model model) {
        LocalDate today = LocalDate.now();
        int y = year != null ? year : (today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1);
        model.addAttribute("year", y);
        model.addAttribute("months", calendarPageService.months(y));
        model.addAttribute("today", today);
        model.addAttribute("enLocale", Locale.ENGLISH);
        return "calendar";
    }
}