package com.ayakovlev.springschool.controller;

import com.ayakovlev.springschool.entity.CourseLesson;
import com.ayakovlev.springschool.entity.SchoolClass;
import com.ayakovlev.springschool.service.SubjectService;
import com.ayakovlev.springschool.service.MarkdownService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/lesson")
public class AdminLessonController {

    private final SubjectService subjectService;
    private final MarkdownService markdownService;

    @GetMapping("/{id}/edit")
    public String form(@PathVariable Long id, Model model) {
        CourseLesson lesson = subjectService.findLesson(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("lesson", lesson);

        model.addAttribute("promptSubject", lesson.getSubject().getName());
        model.addAttribute("promptClasses", lesson.getSubject().getClasses().stream()
                .map(SchoolClass::getGrade).distinct().sorted()
                .map(String::valueOf).collect(Collectors.joining("/")));
        model.addAttribute("promptLanguage", lesson.getSubject().getLanguage().toUpperCase());
        model.addAttribute("promptPrevTopic", subjectService.previousTopic(lesson));

        return "admin/lesson-form";
    }

    @PostMapping("/{id}")
    public String save(@PathVariable Long id,
                       @RequestParam(required = false) String topic,
                       @RequestParam(required = false) String comment,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate actualDate,
                       @RequestParam(required = false) String lecture,
                       @RequestParam(required = false) String classworkStart,
                       @RequestParam(required = false) String classworkJoint,
                       @RequestParam(required = false) String classworkEnd,
                       @RequestParam(required = false) String homework) {
        subjectService.updateLesson(id, topic, comment, actualDate, lecture,
                classworkStart, classworkJoint, classworkEnd, homework);
        return "redirect:/lesson/" + id;
    }

    @PostMapping(value = "/preview", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String preview(@RequestParam(required = false) String text) {
        return markdownService.toHtml(text);
    }
}