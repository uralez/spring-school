package com.ayakovlev.springschool.controller;

import com.ayakovlev.springschool.entity.Subject;
import com.ayakovlev.springschool.service.AccessService;
import com.ayakovlev.springschool.service.ReflowService;
import com.ayakovlev.springschool.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
    private final AccessService access;
    private final ReflowService reflowService;

    @GetMapping("/subject/{id}")
    public String subject(@PathVariable Long id, Model model, Authentication authentication) {
        Subject subject = subjectService.subject(id);
        model.addAttribute("subject", subject);
        model.addAttribute("name", subject.getName());
        model.addAttribute("className", subject.getClasses().isEmpty() ? "" : subject.getClasses().get(0).getClassName());
        model.addAttribute("canEdit", access.canEditSubject(authentication, subject.getId()));
        model.addAttribute("canApprove", access.canApprove(authentication));
        model.addAttribute("plan", subjectService.buildPlan(subject));
        model.addAttribute("currentId", subjectService.currentLessonId(subject.getId(), LocalDate.now()));
//        model.addAttribute("offDays", subjectService.offDays(subject.getId()));
        model.addAttribute("swapRules", subjectService.swapRules(subject.getId()));
        model.addAttribute("needsReflow", reflowService.needsReflow(subject.getId()));
        model.addAttribute("undated", subjectService.undated(subject.getId()));
        model.addAttribute("enLocale", java.util.Locale.ENGLISH);
        return "subject";
    }

    @GetMapping("/subject/{name}/{className}")
    public String legacy(@PathVariable String name, @PathVariable String className) {
        return subjectService.find(name, className, LocalDate.now())
                .map(s -> "redirect:/subject/" + s.getId())
                .orElse("redirect:/");
    }
}