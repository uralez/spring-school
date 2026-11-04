package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.dto.PlanItem;
import com.ayakovlev.springschool.entity.AcademicModule;
import com.ayakovlev.springschool.entity.CourseLesson;
import com.ayakovlev.springschool.entity.SchoolClass;
import com.ayakovlev.springschool.entity.Subject;
import com.ayakovlev.springschool.repository.AcademicModuleRepository;
import com.ayakovlev.springschool.repository.CourseLessonRepository;
import com.ayakovlev.springschool.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final AcademicModuleRepository academicModuleRepository;

    @Transactional(readOnly = true)
    public Optional<Subject> find(String name, String className, LocalDate today) {
        int year = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
        return subjectRepository.findByNameYearAndClass(name, year, className);
    }

    @Transactional(readOnly = true)
    public List<CourseLesson> lessons(Long subjectId) {
        return courseLessonRepository.findBySubjectIdOrderByLessonNumber(subjectId);
    }

    @Transactional(readOnly = true)
    public List<PlanItem> buildPlan(Subject subject) {
        List<CourseLesson> lessons = courseLessonRepository.findBySubjectIdOrderByLessonNumber(subject.getId());
        long shift = subject.getAcademicYear() - 2026L;
        List<AcademicModule> modules = academicModuleRepository.findAll(Sort.by("moduleNumber"));
        List<PlanItem> items = new ArrayList<>();
        int weekNumber = 0;
        for (int i = 0; i < modules.size(); i++) {
            AcademicModule m = modules.get(i);
            LocalDate start = m.getStartDate().plusYears(shift);
            LocalDate end = m.getEndDate().plusYears(shift);
            items.add(PlanItem.module(m.getModuleNumber()));
            Map<LocalDate, List<CourseLesson>> byWeek = lessons.stream()
                    .filter(l -> l.getPlannedDate() != null
                            && !l.getPlannedDate().isBefore(start) && !l.getPlannedDate().isAfter(end))
                    .collect(Collectors.groupingBy(
                            l -> l.getPlannedDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                            LinkedHashMap::new, Collectors.toList()));
            for (List<CourseLesson> week : byWeek.values()) {
                items.add(PlanItem.week(++weekNumber, week));
            }
            if (i + 1 < modules.size()) {
                LocalDate nextStart = modules.get(i + 1).getStartDate().plusYears(shift);
                if (nextStart.isAfter(end.plusDays(1))) {
                    items.add(PlanItem.vacation(end.plusDays(1), nextStart.minusDays(1)));
                }
            }
        }
        return items;
    }

    @Transactional(readOnly = true)
    public Long currentLessonId(Long subjectId, LocalDate today) {
        return courseLessonRepository.findBySubjectIdOrderByLessonNumber(subjectId).stream()
                .filter(l -> l.getPlannedDate() != null && !l.getPlannedDate().isAfter(today))
                .reduce((first, second) -> second)
                .map(CourseLesson::getId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Set<String> plannedKeys(LocalDate today) {
        int year = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
        Set<String> keys = new HashSet<>();
        for (Subject s : subjectRepository.findByAcademicYear(year)) {
            for (SchoolClass c : s.getClasses()) {
                keys.add(s.getName() + "|" + c.getClassName());
            }
        }
        return keys;
    }

    @Transactional(readOnly = true)
    public Optional<CourseLesson> findLesson(Long id) {
        return courseLessonRepository.findById(id);
    }

    @Transactional
    public CourseLesson updateLesson(Long id, String topic, String comment, LocalDate actualDate,
                                     String lecture, String classworkStart, String classworkJoint,
                                     String classworkEnd, String homework) {
        CourseLesson lesson = courseLessonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        lesson.setTopic(blankToNull(topic));
        lesson.setComment(blankToNull(comment));
        lesson.setActualDate(actualDate);
        lesson.setLecture(blankToNull(lecture));
        lesson.setClassworkStart(blankToNull(classworkStart));
        lesson.setClassworkJoint(blankToNull(classworkJoint));
        lesson.setClassworkEnd(blankToNull(classworkEnd));
        lesson.setHomework(blankToNull(homework));
        return lesson;
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    @Transactional(readOnly = true)
    public LocalDate nextLessonDate(CourseLesson lesson) {
        return courseLessonRepository
                .findBySubjectIdAndLessonNumber(lesson.getSubject().getId(), lesson.getLessonNumber() + 1)
                .map(CourseLesson::getPlannedDate)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public String previousTopic(CourseLesson lesson) {
        if (lesson.getLessonNumber() <= 1) {
            return null;
        }
        return courseLessonRepository
                .findBySubjectIdAndLessonNumber(lesson.getSubject().getId(), lesson.getLessonNumber() - 1)
                .map(CourseLesson::getTopic)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Subject> subjects(LocalDate today) {
        int year = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
        return subjectRepository.findByAcademicYear(year);
    }

    @Transactional(readOnly = true)
    public Subject subject(Long id) {
        return subjectRepository.findById(id).orElseThrow();
    }
}