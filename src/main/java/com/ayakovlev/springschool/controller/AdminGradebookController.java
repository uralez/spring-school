package com.ayakovlev.springschool.controller;

import com.ayakovlev.springschool.entity.Subject;
import com.ayakovlev.springschool.service.GradeService;
import com.ayakovlev.springschool.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/grades")
public class AdminGradebookController {

    private final SubjectService subjectService;
    private final GradeService gradeService;

    @GetMapping
    public String gradebook(@RequestParam(required = false) Long subjectId, Model model) {
        LocalDate today = LocalDate.now();
        List<Subject> subjects = subjectService.subjects(today);
        model.addAttribute("subjects", subjects);
        if (!subjects.isEmpty()) {
            Subject subject = subjectId == null ? subjects.get(0) : subjectService.subject(subjectId);
            model.addAttribute("book", gradeService.gradebook(subject, today));
        }
        model.addAttribute("enLocale", Locale.ENGLISH);
        return "admin/gradebook";
    }
}