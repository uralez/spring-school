package com.ayakovlev.springschool.controller;

import com.ayakovlev.springschool.entity.CourseLesson;
import com.ayakovlev.springschool.service.MarkdownService;
import com.ayakovlev.springschool.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class LessonController {

    private final SubjectService subjectService;
    private final MarkdownService markdownService;

    @GetMapping("/lesson/{id}")
    public String lesson(@PathVariable Long id, Model model) {
        CourseLesson lesson = subjectService.findLesson(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("lesson", lesson);
        model.addAttribute("subject", lesson.getSubject());
        model.addAttribute("lectureHtml", markdownService.toHtml(lesson.getLecture()));
        model.addAttribute("classworkStartHtml", markdownService.toHtml(lesson.getClassworkStart()));
        model.addAttribute("classworkJointHtml", markdownService.toHtml(lesson.getClassworkJoint()));
        model.addAttribute("classworkEndHtml", markdownService.toHtml(lesson.getClassworkEnd()));
        model.addAttribute("homeworkHtml", markdownService.toHtml(lesson.getHomework()));
        model.addAttribute("enLocale", java.util.Locale.ENGLISH);
        return "lesson";
    }
}