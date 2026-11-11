package com.ayakovlev.springschool.controller;

import com.ayakovlev.springschool.entity.CourseLesson;
import com.ayakovlev.springschool.entity.Student;
import com.ayakovlev.springschool.service.GradeService;
import com.ayakovlev.springschool.service.MarkdownService;
import com.ayakovlev.springschool.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manage/lesson")
public class PrintController {

    private final SubjectService subjectService;
    private final GradeService gradeService;
    private final MarkdownService markdownService;

    @GetMapping("/{id}/print")
    @PreAuthorize("@access.canEditLesson(authentication, #id)")
    public String print(@PathVariable Long id,
                        @RequestParam(defaultValue = "spring-school") String place,
                        @RequestParam(defaultValue = "") String subject,
                        @RequestParam(defaultValue = "") String className,
                        @RequestParam(defaultValue = "") String date,
                        @RequestParam(defaultValue = "") String topic,
                        @RequestParam(required = false) List<Long> studentIds,
                        @RequestParam(defaultValue = "0") int blanks,
                        Model model) {
        CourseLesson lesson = subjectService.findLesson(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Set<Long> chosen = studentIds == null ? Set.of() : new HashSet<>(studentIds);
        List<String> names = new ArrayList<>();
        for (Student s : gradeService.studentsFor(lesson)) {
            if (chosen.contains(s.getId())) {
                names.add(s.getUser().getFullName());
            }
        }
        for (int i = 0; i < Math.min(Math.max(blanks, 0), 50); i++) {
            names.add("");
        }
        model.addAttribute("place", place);
        model.addAttribute("subject", subject);
        model.addAttribute("className", className);
        model.addAttribute("date", date);
        model.addAttribute("topic", topic);
        model.addAttribute("names", names);
        model.addAttribute("startHtml", markdownService.toHtml(lesson.getClassworkStart()));
        model.addAttribute("endHtml", markdownService.toHtml(lesson.getClassworkEnd()));
        model.addAttribute("homeworkHtml", markdownService.toHtml(lesson.getHomework()));
        return "print-sheets";
    }
}