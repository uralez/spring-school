package com.ayakovlev.springschool.controller;

import com.ayakovlev.springschool.entity.Subject;
import com.ayakovlev.springschool.service.ReflowService;
import com.ayakovlev.springschool.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manage/subject")
public class ManageSubjectController {

    private final SubjectService subjectService;
    private final ReflowService reflowService;

    @PostMapping("/{id}/textbook")
    @PreAuthorize("@access.canEditSubject(authentication, #id)")
    public String saveTextbook(@PathVariable Long id,
                               @RequestParam String title,
                               @RequestParam(required = false) String authors,
                               @RequestParam(required = false) String publisher,
                               @RequestParam(required = false) Integer publicationYear,
                               @RequestParam(required = false) String isbn,
                               @RequestParam(required = false) String publisherUrl,
                               @RequestParam(required = false) String fileUrl) {
        subjectService.saveTextbook(id, title, authors, publisher, publicationYear, isbn, publisherUrl, fileUrl);
        Subject subject = subjectService.subject(id);
        return "redirect:/subject/" + subject.getName() + "/" + subject.getClasses().get(0).getClassName();
    }

    @PostMapping("/{id}/approval")
    @PreAuthorize("@access.canApprove(authentication)")
    public String approve(@PathVariable Long id,
                          @RequestParam String approvedBy,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate approvedOn) {
        subjectService.approve(id, approvedBy, approvedOn);
        Subject subject = subjectService.subject(id);
        return "redirect:/subject/" + subject.getName() + "/" + subject.getClasses().get(0).getClassName();
    }

    @PostMapping("/{id}/reflow")
    @PreAuthorize("@access.canEditSubject(authentication, #id)")
    public String reflow(@PathVariable Long id) {
        reflowService.reflow(id);
        Subject subject = subjectService.subject(id);
        return "redirect:/subject/" + subject.getName() + "/" + subject.getClasses().get(0).getClassName();
    }

    @PostMapping("/{id}/lessons/{lessonId}/swap")
    @PreAuthorize("@access.canEditSubject(authentication, #id)")
    public String swap(@PathVariable Long id, @PathVariable Long lessonId, @RequestParam int delta) {
        subjectService.swapWithNeighbor(id, lessonId, delta);
        Subject subject = subjectService.subject(id);
        return "redirect:/subject/" + subject.getName() + "/" + subject.getClasses().get(0).getClassName()
                + "#lesson-" + lessonId;
    }

    @PostMapping("/{id}/lessons/{lessonId}/delete")
    @PreAuthorize("@access.canEditSubject(authentication, #id)")
    public String deleteLesson(@PathVariable Long id, @PathVariable Long lessonId) {
        subjectService.deleteLesson(lessonId);
        Subject subject = subjectService.subject(id);
        return "redirect:/subject/" + subject.getName() + "/" + subject.getClasses().get(0).getClassName();
    }
}