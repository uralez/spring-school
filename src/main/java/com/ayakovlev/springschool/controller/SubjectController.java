package com.ayakovlev.springschool.controller;

import com.ayakovlev.springschool.entity.Subject;
import com.ayakovlev.springschool.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping("/subject/{name}/{className}")
    public String subject(@PathVariable String name, @PathVariable String className, Model model) {
        Optional<Subject> subject = subjectService.find(name, className, LocalDate.now());
        model.addAttribute("name", name);
        model.addAttribute("className", className);
        if (subject.isPresent()) {
            model.addAttribute("subject", subject.get());
            model.addAttribute("plan", subjectService.buildPlan(subject.get()));
            model.addAttribute("currentId", subjectService.currentLessonId(subject.get().getId(), LocalDate.now()));
            model.addAttribute("enLocale", java.util.Locale.ENGLISH);
        }
        return "subject";
    }
}