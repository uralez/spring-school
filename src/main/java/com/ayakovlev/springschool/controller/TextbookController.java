package com.ayakovlev.springschool.controller;

import com.ayakovlev.springschool.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class TextbookController {

    private final SubjectService subjectService;

    @GetMapping("/textbooks")
    public String textbooks(Model model, Authentication authentication) {
        model.addAttribute("groups", subjectService.textbooksByClass(LocalDate.now()));
        model.addAttribute("staff", authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> java.util.Set.of("ROLE_ADMIN", "ROLE_DIRECTOR", "ROLE_DEPUTY",
                        "ROLE_METHODIST", "ROLE_TEACHER").contains(a.getAuthority())));
        return "textbooks";
    }
}