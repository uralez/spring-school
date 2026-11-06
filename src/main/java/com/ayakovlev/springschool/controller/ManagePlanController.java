package com.ayakovlev.springschool.controller;

import com.ayakovlev.springschool.entity.Lesson;
import com.ayakovlev.springschool.entity.Subject;
import com.ayakovlev.springschool.repository.LessonRepository;
import com.ayakovlev.springschool.service.PlanGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manage/plan")
public class ManagePlanController {

    private final LessonRepository lessonRepository;
    private final PlanGeneratorService planGeneratorService;

    @PostMapping("/create/{lessonId}")
    @PreAuthorize("@access.canEditPlans(authentication)")
//    @Transactional(readOnly = true)
    public String create(@PathVariable Integer lessonId, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        LocalDate today = LocalDate.now();
        int year = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
        String language = lesson.getSubjectName().matches(".*[а-яА-Я].*") ? "ru" : "en";
        Subject subject = planGeneratorService.create(lesson, year, language, today, username);
        String className = subject.getClasses().get(0).getClassName();
        return "redirect:/subject/" + subject.getName() + "/" + className;
    }
}