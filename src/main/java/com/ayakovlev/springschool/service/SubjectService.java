package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.dto.PlanItem;
import com.ayakovlev.springschool.entity.*;
import com.ayakovlev.springschool.repository.*;
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
    private final TextbookRepository textbookRepository;
    private final StudentGradeRepository studentGradeRepository;
    private final TopicListParser topicListParser;

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

    @Transactional
    public void saveTextbook(Long subjectId, String title, String authors, String publisher,
                             Integer year, String isbn, String publisherUrl, String fileUrl) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        Textbook textbook = subject.getTextbook() != null ? subject.getTextbook() : new Textbook();
        textbook.setTitle(title.trim());
        textbook.setAuthors(blank(authors));
        textbook.setPublisher(blank(publisher));
        textbook.setPublicationYear(year);
        textbook.setIsbn(blank(isbn));
        textbook.setPublisherUrl(blank(publisherUrl));
        textbook.setFileUrl(blank(fileUrl));
        textbookRepository.save(textbook);
        subject.setTextbook(textbook);
    }

    private String blank(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    @Transactional(readOnly = true)
    public Optional<CourseLesson> neighbor(CourseLesson lesson, int delta) {
        return courseLessonRepository.findBySubjectIdAndLessonNumber(
                lesson.getSubject().getId(), lesson.getLessonNumber() + delta);
    }

    @Transactional
    public void approve(Long subjectId, String approvedBy, LocalDate approvedOn) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        subject.setApprovedBy(approvedBy == null || approvedBy.isBlank() ? null : approvedBy.trim());
        subject.setApprovedOn(approvedOn);
    }

    public record SwapRules(Set<Long> up, Set<Long> down) {
    }

    @Transactional(readOnly = true)
    public SwapRules swapRules(Long subjectId) {
        List<CourseLesson> list = courseLessonRepository.findBySubjectIdOrderByLessonNumber(subjectId);
        Set<Long> graded = studentGradeRepository.gradedLessonIds(subjectId);
        Set<Long> up = new HashSet<>();
        Set<Long> down = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            if (graded.contains(list.get(i).getId())) {
                continue;
            }
            if (i > 0 && !graded.contains(list.get(i - 1).getId())) {
                up.add(list.get(i).getId());
            }
            if (i < list.size() - 1 && !graded.contains(list.get(i + 1).getId())) {
                down.add(list.get(i).getId());
            }
        }
        return new SwapRules(up, down);
    }

    @Transactional
    public void swapWithNeighbor(Long subjectId, Long lessonId, int delta) {
        CourseLesson a = courseLessonRepository.findById(lessonId).orElseThrow();
        if (!a.getSubject().getId().equals(subjectId) || Math.abs(delta) != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        CourseLesson b = courseLessonRepository.findBySubjectIdAndLessonNumber(
                subjectId, a.getLessonNumber() + delta).orElseThrow();
        Set<Long> graded = studentGradeRepository.gradedLessonIds(subjectId);
        if (graded.contains(a.getId()) || graded.contains(b.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        String type = a.getLessonType();
        String topic = a.getTopic();
        String comment = a.getComment();
        String lecture = a.getLecture();
        String cs = a.getClassworkStart();
        String cj = a.getClassworkJoint();
        String ce = a.getClassworkEnd();
        String hw = a.getHomework();

        a.setLessonType(b.getLessonType());
        a.setTopic(b.getTopic());
        a.setComment(b.getComment());
        a.setLecture(b.getLecture());
        a.setClassworkStart(b.getClassworkStart());
        a.setClassworkJoint(b.getClassworkJoint());
        a.setClassworkEnd(b.getClassworkEnd());
        a.setHomework(b.getHomework());

        b.setLessonType(type);
        b.setTopic(topic);
        b.setComment(comment);
        b.setLecture(lecture);
        b.setClassworkStart(cs);
        b.setClassworkJoint(cj);
        b.setClassworkEnd(ce);
        b.setHomework(hw);
    }

    @Transactional(readOnly = true)
    public List<CourseLesson> undated(Long subjectId) {
        return courseLessonRepository.findBySubjectIdOrderByLessonNumber(subjectId).stream()
                .filter(l -> l.getPlannedDate() == null).toList();
    }

    @Transactional(readOnly = true)
    public long gradeCount(Long lessonId) {
        return studentGradeRepository.countByCourseLessonId(lessonId);
    }

    @Transactional
    public Long deleteLesson(Long lessonId) {
        CourseLesson lesson = courseLessonRepository.findById(lessonId).orElseThrow();
        if (lesson.getPlannedDate() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        Long subjectId = lesson.getSubject().getId();
        courseLessonRepository.delete(lesson);
        courseLessonRepository.flush();
        courseLessonRepository.renumberStep1(subjectId);
        courseLessonRepository.renumberStep2(subjectId);
        return subjectId;
    }

    public record ImportResult(int assigned, List<String> extra) {
    }

    @Transactional
    public ImportResult importTopics(Long subjectId, String text) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        List<String> topics = topicListParser.parse(text);
        List<CourseLesson> empty = courseLessonRepository.findBySubjectIdOrderByLessonNumber(subjectId).stream()
                .filter(l -> "lesson".equals(l.getLessonType()))
                .filter(l -> l.getTopic() == null || l.getTopic().isBlank())
                .toList();
        int n = Math.min(topics.size(), empty.size());
        for (int i = 0; i < n; i++) {
            empty.get(i).setTopic(topics.get(i));
        }
        List<String> extra = new ArrayList<>(topics.subList(n, topics.size()));
        List<CourseLesson> created = new ArrayList<>();
        int temp = 900000;
        for (String topic : extra) {
            CourseLesson l = new CourseLesson();
            l.setSubject(subject);
            l.setLessonType("lesson");
            l.setTopic(topic);
            l.setLessonNumber(++temp);
            created.add(l);
        }
        courseLessonRepository.saveAll(created);
        if (!created.isEmpty()) {
            courseLessonRepository.renumberStep1(subjectId);
            courseLessonRepository.renumberStep2(subjectId);
        }
        return new ImportResult(n, extra);
    }

    @Transactional
    public Long clearLesson(Long lessonId) {
        CourseLesson lesson = courseLessonRepository.findById(lessonId).orElseThrow();
        lesson.setTopic(null);
        lesson.setComment(null);
        lesson.setLecture(null);
        lesson.setClassworkStart(null);
        lesson.setClassworkJoint(null);
        lesson.setClassworkEnd(null);
        lesson.setHomework(null);
        return lesson.getSubject().getId();
    }
}