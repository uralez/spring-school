package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.dto.Gradebook;
import com.ayakovlev.springschool.entity.*;
import com.ayakovlev.springschool.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService {

    private static final Map<String, String[]> PREFIX = Map.of(
            "classwork_start", new String[]{"Independent work (start)", "Самостоятельная (начало)"},
            "classwork_end", new String[]{"Independent work (end)", "Самостоятельная (конец)"},
            "homework", new String[]{"Homework", "Домашняя работа"},
            "board", new String[]{"Board work", "Работа у доски"},
            "test", new String[]{"Test", "Контрольная"},
            "retake", new String[]{"Retake", "Пересдача"});

    private final GroupMembershipRepository groupMembershipRepository;
    private final StudentGradeRepository studentGradeRepository;
    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;

    @Transactional(readOnly = true)
    public List<Student> studentsFor(Lesson lesson) {
        Set<String> names = lesson.getSubject().getClasses().stream()
                .map(SchoolClass::getClassName).collect(Collectors.toSet());
        return groupMembershipRepository.findByGroupNameInAndActiveTrueOrderByStudentUserFullName(names)
                .stream().map(GroupMembership::getStudent).toList();
    }

    @Transactional(readOnly = true)
    public List<StudentGrade> gradesFor(Long lessonId) {
        return studentGradeRepository.findByLessonIdOrderByDcre(lessonId);
    }

    @Transactional
    public void addGrade(Long lessonId, Long studentId, String kind, int score, String comment) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();
        Student student = studentRepository.findById(studentId).orElseThrow();
        StudentGrade grade = new StudentGrade();
        grade.setStudent(student);
        grade.setLesson(lesson);
        grade.setScore(score);
        grade.setKind(kind == null || kind.isBlank() ? null : kind);
        grade.setComment(buildComment(lesson.getSubject().getLanguage(), kind, comment));
        studentGradeRepository.save(grade);
    }

    private String buildComment(String language, String kind, String comment) {
        String text = comment == null ? "" : comment.trim();
        String[] prefixes = PREFIX.get(kind);
        if (prefixes == null) {
            return text.isEmpty() ? null : text;
        }
        String prefix = "ru".equals(language) ? prefixes[1] : prefixes[0];
        return text.isEmpty() ? prefix : prefix + ": " + text;
    }

    @Transactional(readOnly = true)
    public Gradebook gradebook(Subject subject, LocalDate today) {
        List<Lesson> lessons = lessonRepository.findBySubjectIdOrderByLessonNumber(subject.getId()).stream()
                .filter(l -> l.getPlannedDate() != null && !l.getPlannedDate().isAfter(today))
                .toList();
        Set<String> names = subject.getClasses().stream()
                .map(SchoolClass::getClassName).collect(Collectors.toSet());
        List<Student> students = groupMembershipRepository
                .findByGroupNameInAndActiveTrueOrderByStudentUserFullName(names).stream()
                .map(GroupMembership::getStudent).toList();

        Map<String, List<Integer>> byCell = new LinkedHashMap<>();
        Set<Long> graded = new HashSet<>();
        for (StudentGrade g : studentGradeRepository.findByLessonSubjectIdOrderByDcre(subject.getId())) {
            byCell.computeIfAbsent(g.getStudent().getId() + ":" + g.getLesson().getId(),
                    k -> new ArrayList<>()).add(g.getScore());
            graded.add(g.getLesson().getId());
        }
        Map<String, String> cells = new HashMap<>();
        byCell.forEach((k, v) -> cells.put(k, v.stream().map(String::valueOf).collect(Collectors.joining(", "))));
        return new Gradebook(subject, lessons, students, cells, graded);
    }

    @Transactional
    public Long updateGrade(Long gradeId, Long studentId, String kind, int score, String comment) {
        StudentGrade grade = studentGradeRepository.findById(gradeId).orElseThrow();
        grade.setStudent(studentRepository.findById(studentId).orElseThrow());
        grade.setKind(kind == null || kind.isBlank() ? null : kind);
        grade.setScore(score);
        String text = comment == null ? "" : comment.trim();
        text = stripPrefix(text);
        String[] prefixes = kind == null ? null : PREFIX.get(kind);
        String lang = grade.getLesson().getSubject().getLanguage();
        if (prefixes != null) {
            String prefix = "ru".equals(lang) ? prefixes[1] : prefixes[0];
            text = text.isEmpty() ? prefix : prefix + ": " + text;
        }
        grade.setComment(text.isEmpty() ? null : text);
        return grade.getLesson().getId();
    }

    private String stripPrefix(String text) {
        for (String[] pair : PREFIX.values()) {
            for (String p : pair) {
                if (text.equals(p)) {
                    return "";
                }
                if (text.startsWith(p + ": ")) {
                    return text.substring(p.length() + 2).trim();
                }
            }
        }
        return text;
    }

    @Transactional
    public Long deleteGrade(Long gradeId) {
        StudentGrade grade = studentGradeRepository.findById(gradeId).orElseThrow();
        Long lessonId = grade.getLesson().getId();
        studentGradeRepository.delete(grade);
        return lessonId;
    }
}