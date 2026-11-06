package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.entity.*;
import com.ayakovlev.springschool.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanGeneratorService {

    private static final int FINAL_REVIEWS = 2;

    private final LessonRepository lessonRepository;
    private final CalendarDayRepository calendarDayRepository;
    private final AcademicModuleRepository academicModuleRepository;
    private final SubjectRepository subjectRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final TeacherRepository teacherRepository;

    private record Slot(LocalDate date, LocalTime time, int module) {
    }

    @Transactional
    public Subject create(Lesson source, int year, String language, LocalDate today, String username) {
        String name = source.getSubjectName();
        List<String> groups = source.getGroups().stream().sorted().toList();
        Optional<Subject> existing = subjectRepository.findByNameYearAndClass(name, year, groups.get(0));
        if (existing.isPresent()) {
            return existing.get();
        }

        List<Lesson> timetable = lessonRepository.findBySubjectAndGroups(name, groups);
        List<AcademicModule> modules = academicModuleRepository.findAll(Sort.by("moduleNumber"));
        int lastModule = modules.get(modules.size() - 1).getModuleNumber();

        List<Slot> slots = new ArrayList<>();
        for (CalendarDay day : calendarDayRepository.findByDayTypeOrderByCalendarDate("working")) {
            LocalDate date = day.getCalendarDate();
            for (Lesson l : timetable) {
                boolean active = l.getDayOfWeek().equals(day.getDayOfWeek())
                        && !l.getValidFrom().isAfter(date)
                        && (l.getValidTo() == null || !l.getValidTo().isBefore(date));
                if (!active) {
                    continue;
                }
                for (AcademicModule m : modules) {
                    if (!date.isBefore(m.getStartDate()) && !date.isAfter(m.getEndDate())) {
                        slots.add(new Slot(date, l.getStartTime(), m.getModuleNumber()));
                    }
                }
            }
        }
        slots.sort(Comparator.comparing(Slot::date).thenComparing(Slot::time));

        Subject subject = new Subject();
        subject.setName(name);
        subject.setAcademicYear(year);
        subject.setLanguage(language);
        for (String g : groups) {
            SchoolClass c = new SchoolClass();
            c.setClassName(g);
            c.setGrade(Integer.parseInt(g.replaceAll("\\..*$", "")));
            subject.getClasses().add(c);
        }
        subjectRepository.save(subject);

        teacherRepository.findByUserUsername(username).ifPresent(t -> subject.getTeachers().add(t));

        Map<Integer, Long> total = slots.stream()
                .collect(Collectors.groupingBy(Slot::module, Collectors.counting()));
        Map<Integer, Integer> seen = new HashMap<>();
        List<CourseLesson> lessons = new ArrayList<>();
        int number = 0;
        for (Slot s : slots) {
            int index = seen.merge(s.module(), 1, Integer::sum);
            int k = (int) (total.get(s.module()) - index + 1);
            String kind = kind(s.module(), k, lastModule);

            CourseLesson lesson = new CourseLesson();
            lesson.setSubject(subject);
            lesson.setLessonNumber(++number);
            lesson.setLessonType(kind.equals("final") ? "review" : kind);
            lesson.setTopic(switch (kind) {
                case "test" -> "Test: module " + s.module();
                case "review" -> "Review: module " + s.module();
                case "final" -> "Final review";
                default -> null;
            });
            lesson.setPlannedDate(s.date());
            if (!s.date().isAfter(today)) {
                lesson.setActualDate(s.date());
            }
            lessons.add(lesson);
        }
        courseLessonRepository.saveAll(lessons);
        return subject;
    }

    private String kind(int module, int k, int lastModule) {
        if (module == lastModule) {
            if (k <= FINAL_REVIEWS) {
                return "final";
            }
            if (k == FINAL_REVIEWS + 2) {
                return "test";
            }
            if (k == FINAL_REVIEWS + 3) {
                return "review";
            }
            return "lesson";
        }
        if (k == 2) {
            return "test";
        }
        if (k == 3) {
            return "review";
        }
        return "lesson";
    }
}