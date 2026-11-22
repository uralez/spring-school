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
    private final TimetableEntryRepository timetableEntryRepository;
    private final CalendarDayRepository calendarDayRepository;
    private final LessonRepository lessonRepository;
    private final LessonSlotRepository lessonSlotRepository;
    private final StudentGradeRepository studentGradeRepository;

    private record Layout(List<LocalDate> available, List<Lesson> movable, List<LocalDate> free) {
    }

    private Layout layout(Subject subject) {
        List<TimetableEntry> timetable = timetableEntryRepository.findBySubjectIdOrderByStartTime(subject.getId());
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
            for (TimetableEntry entry : timetable) {
                boolean active = entry.getDayOfWeek().equals(day.getDayOfWeek())
                        && !entry.getValidFrom().isAfter(date)
                        && (entry.getValidTo() == null || !entry.getValidTo().isBefore(date));
                if (active) {
                    available.add(date);
                }
            }
        }
        Collections.sort(available);

        Set<Long> graded = studentGradeRepository.gradedLessonIds(subject.getId());
        List<Lesson> movable = new ArrayList<>();
        LocalDate lastFixed = LocalDate.MIN;
        for (Lesson l : lessonRepository.findBySubjectIdOrderByLessonNumber(subject.getId())) {
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
            log.warn("REFLOW: free=" + layout.free().size() + " movable=" + layout.movable().size() + ", subject: " + subject.getName() + "-" + subject.getClasses());
            //System.out.println("REFLOW: free=" + layout.free().size() + " movable=" + layout.movable().size());
            return true;
        }
        for (int i = 0; i < layout.free().size(); i++) {
            if (!layout.free().get(i).equals(layout.movable().get(i).getPlannedDate())) {
                log.warn("REFLOW: difference at " + i + ": free=" + layout.free().get(i)
                        + ", lesson=" + layout.movable().get(i).getPlannedDate() + ", subject: " + subject.getName() + "-" + subject.getClasses());
//                System.out.println("REFLOW: difference at " + i + ": free=" + layout.free().get(i)
//                        + ", lesson=" + layout.movable().get(i).getPlannedDate());
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
        List<LessonSlot> existing = lessonSlotRepository.findBySubjectIdOrderBySlotNumber(subjectId);
        Map<LocalDate, LessonSlot> byDate = new HashMap<>();
        for (LessonSlot s : existing) {
            byDate.put(s.getSlotDate(), s);
        }
        List<LocalDate> free = layout.free();

        for (Lesson l : layout.movable()) {
            l.setSlot(null);
//            l.setPlannedDate(null);
        }
        lessonRepository.flush();

        Set<LocalDate> keep = new HashSet<>(free);
        for (Lesson l : lessonRepository.findBySubjectIdOrderByLessonNumber(subjectId)) {
            if (l.getSlot() != null) {
                keep.add(l.getSlot().getSlotDate());
            }
        }
        for (LessonSlot s : existing) {
            if (!keep.contains(s.getSlotDate())) {
                lessonSlotRepository.delete(s);
            }
        }
        lessonSlotRepository.flush();

        Map<LocalDate, LessonSlot> slots = new HashMap<>();
        for (LessonSlot s : lessonSlotRepository.findBySubjectIdOrderBySlotNumber(subjectId)) {
            slots.put(s.getSlotDate(), s);
        }
        List<LessonSlot> fresh = new ArrayList<>();
        for (LocalDate d : free) {
            if (!slots.containsKey(d)) {
                LessonSlot s = new LessonSlot();
                s.setSubject(subject);
                s.setSlotDate(d);
                s.setSlotNumber(900000 + fresh.size());
                fresh.add(s);
                slots.put(d, s);
            }
        }
        lessonSlotRepository.saveAll(fresh);

        int i = 0;
        for (Lesson l : layout.movable()) {
            if (i < free.size()) {
                LessonSlot s = slots.get(free.get(i++));
                l.setSlot(s);
//                l.setPlannedDate(s.getSlotDate());
            }
        }
        List<Lesson> created = new ArrayList<>();
        int temp = 900000;
        while (i < free.size()) {
            LessonSlot s = slots.get(free.get(i++));
            Lesson l = new Lesson();
            l.setSubject(subject);
            l.setLessonType("lesson");
            l.setSlot(s);
//            l.setPlannedDate(s.getSlotDate());
            l.setLessonNumber(++temp);
            created.add(l);
        }
        lessonRepository.saveAll(created);
        lessonRepository.renumberStep1(subjectId);
        lessonRepository.renumberStep2(subjectId);

        lessonSlotRepository.renumberStep1(subjectId);
        lessonSlotRepository.renumberStep2(subjectId);
    }
}