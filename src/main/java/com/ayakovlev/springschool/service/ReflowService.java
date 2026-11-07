package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.entity.*;
import com.ayakovlev.springschool.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReflowService {

    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;
    private final CalendarDayRepository calendarDayRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final LessonSlotRepository lessonSlotRepository;
    private final StudentGradeRepository studentGradeRepository;

    private record Layout(List<LocalDate> available, List<CourseLesson> movable, List<LocalDate> free) {
    }

    private Layout layout(Subject subject) {
        List<String> groups = subject.getClasses().stream().map(SchoolClass::getClassName).toList();
        List<Lesson> timetable = lessonRepository.findBySubjectAndGroups(subject.getName(), groups);
        if (timetable.isEmpty()) {
            return null;
        }
        int year = subject.getAcademicYear();
        List<LocalDate> available = new ArrayList<>();
        for (CalendarDay day : calendarDayRepository
                .findByCalendarDateBetween(LocalDate.of(year, 9, 1), LocalDate.of(year + 1, 8, 31))) {
            if (!"working".equals(day.getDayType())) {
                continue;
            }
            LocalDate date = day.getCalendarDate();
            for (Lesson l : timetable) {
                boolean active = l.getDayOfWeek().equals(day.getDayOfWeek())
                        && !l.getValidFrom().isAfter(date)
                        && (l.getValidTo() == null || !l.getValidTo().isBefore(date));
                if (active) {
                    available.add(date);
                }
            }
        }
        Collections.sort(available);

        Set<Long> graded = studentGradeRepository.gradedLessonIds(subject.getId());
        List<CourseLesson> movable = new ArrayList<>();
        LocalDate lastFixed = LocalDate.MIN;
        for (CourseLesson l : courseLessonRepository.findBySubjectIdOrderByLessonNumber(subject.getId())) {
            boolean fixed = l.getActualDate() != null || graded.contains(l.getId());
            if (!fixed) {
                movable.add(l);
            } else if (l.getPlannedDate() != null && l.getPlannedDate().isAfter(lastFixed)) {
                lastFixed = l.getPlannedDate();
            }
        }
        LocalDate boundary = lastFixed;
        List<LocalDate> free = available.stream().filter(d -> d.isAfter(boundary)).toList();
        return new Layout(available, movable, free);
    }

    @Transactional(readOnly = true)
    public boolean needsReflow(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        Layout layout = layout(subject);
        if (layout == null) {
            return false;
        }
        if (layout.free().size() != layout.movable().size()) {
            log.warn("REFLOW: free=" + layout.free().size() + " movable=" + layout.movable().size());
            System.out.println("REFLOW: free=" + layout.free().size() + " movable=" + layout.movable().size());
            return true;
        }
        for (int i = 0; i < layout.free().size(); i++) {
            if (!layout.free().get(i).equals(layout.movable().get(i).getPlannedDate())) {
                log.warn("REFLOW: difference at " + i + ": free=" + layout.free().get(i)
                        + ", lesson=" + layout.movable().get(i).getPlannedDate());
                System.out.println("REFLOW: difference at " + i + ": free=" + layout.free().get(i)
                        + ", lesson=" + layout.movable().get(i).getPlannedDate());
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void reflow(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        Layout layout = layout(subject);
        if (layout == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        List<LocalDate> free = layout.free();

        int i = 0;
        for (CourseLesson l : layout.movable()) {
            l.setPlannedDate(i < free.size() ? free.get(i++) : null);
        }
        List<CourseLesson> created = new ArrayList<>();
        int temp = 900000;
        while (i < free.size()) {
            CourseLesson l = new CourseLesson();
            l.setSubject(subject);
            l.setLessonType("lesson");
            l.setPlannedDate(free.get(i++));
            l.setLessonNumber(++temp);
            created.add(l);
        }
        courseLessonRepository.saveAll(created);
        courseLessonRepository.renumberStep1(subjectId);
        courseLessonRepository.renumberStep2(subjectId);

        lessonSlotRepository.deleteBySubject(subjectId);
        Subject fresh = subjectRepository.findById(subjectId).orElseThrow();
        List<LessonSlot> slots = new ArrayList<>();
        int n = 0;
        for (LocalDate d : layout.available()) {
            LessonSlot s = new LessonSlot();
            s.setSubject(fresh);
            s.setSlotNumber(++n);
            s.setSlotDate(d);
            slots.add(s);
        }
        lessonSlotRepository.saveAll(slots);
    }
}