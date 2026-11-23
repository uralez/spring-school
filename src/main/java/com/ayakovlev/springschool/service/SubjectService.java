package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.dto.PlanItem;
import com.ayakovlev.springschool.dto.TextbookEntry;
import com.ayakovlev.springschool.dto.TextbookGroup;
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
    private final LessonRepository lessonRepository;
    private final AcademicModuleRepository academicModuleRepository;
    private final TextbookRepository textbookRepository;
    private final StudentGradeRepository studentGradeRepository;
    private final TopicListParser topicListParser;
    private final CalendarDayRepository calendarDayRepository;

//    @Transactional(readOnly = true)
//    public Optional<Subject> find(String name, String className, LocalDate today) {
//        int year = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
//        return subjectRepository.findByNameYearAndClass(name, year, className);
//    }

    @Transactional(readOnly = true)
    public List<Lesson> lessons(Long subjectId) {
        return lessonRepository.findBySubjectIdOrderByLessonNumber(subjectId);
    }

    @Transactional(readOnly = true)
    public List<PlanItem> buildPlan(Subject subject) {
        List<Lesson> lessons = lessonRepository.findBySubjectIdOrderByLessonNumber(subject.getId());
        long shift = subject.getAcademicYear() - 2026L;
        List<AcademicModule> modules = academicModuleRepository.findAll(Sort.by("moduleNumber"));
        List<PlanItem> items = new ArrayList<>();
        int weekNumber = 0;
        for (int i = 0; i < modules.size(); i++) {
            AcademicModule m = modules.get(i);
            LocalDate start = m.getStartDate().plusYears(shift);
            LocalDate end = m.getEndDate().plusYears(shift);
            items.add(PlanItem.module(m.getModuleNumber()));
            Map<LocalDate, List<Lesson>> byWeek = lessons.stream()
                    .filter(l -> l.getPlannedDate() != null
                            && !l.getPlannedDate().isBefore(start) && !l.getPlannedDate().isAfter(end))
                    .collect(Collectors.groupingBy(
                            l -> l.getPlannedDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                            LinkedHashMap::new, Collectors.toList()));
            for (List<Lesson> week : byWeek.values()) {
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
        return lessonRepository.findBySubjectIdOrderByLessonNumber(subjectId).stream()
                .filter(l -> l.getPlannedDate() != null && !l.getPlannedDate().isAfter(today))
                .reduce((first, second) -> second)
                .map(Lesson::getId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Set<Long> plannedSubjectIds() {
        return lessonRepository.findSubjectIdsWithLessons();
    }

    @Transactional(readOnly = true)
    public Optional<Lesson> findLesson(Long id) {
        return lessonRepository.findById(id);
    }

    @Transactional
    public Lesson updateLesson(Long id, String topic, String comment, LocalDate actualDate,
                               String lecture, String classworkStart, String classworkJoint,
                               String classworkEnd, String homework) {
        Lesson lesson = lessonRepository.findById(id)
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
    public LocalDate nextLessonDate(Lesson lesson) {
        return lessonRepository
                .findBySubjectIdAndLessonNumber(lesson.getSubject().getId(), lesson.getLessonNumber() + 1)
                .map(Lesson::getPlannedDate)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public String previousTopic(Lesson lesson) {
        if (lesson.getLessonNumber() <= 1) {
            return null;
        }
        return lessonRepository
                .findBySubjectIdAndLessonNumber(lesson.getSubject().getId(), lesson.getLessonNumber() - 1)
                .map(Lesson::getTopic)
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
                             Integer year, String isbn, String publisherUrl, String fileUrl,
                             Integer yearsDesigned) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        Textbook textbook = subject.getTextbook() != null ? subject.getTextbook() : new Textbook();
        textbook.setTitle(title.trim());
        textbook.setAuthors(blank(authors));
        textbook.setPublisher(blank(publisher));
        textbook.setPublicationYear(year);
        textbook.setIsbn(blank(isbn));
        textbook.setPublisherUrl(blank(publisherUrl));
        textbook.setFileUrl(blank(fileUrl));
        textbook.setYearsDesigned(yearsDesigned);
        textbookRepository.save(textbook);
        subject.setTextbook(textbook);
    }

    private String blank(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    @Transactional(readOnly = true)
    public Optional<Lesson> neighbor(Lesson lesson, int delta) {
        return lessonRepository.findBySubjectIdAndLessonNumber(
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
        List<Lesson> list = lessonRepository.findBySubjectIdOrderByLessonNumber(subjectId);
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
        Lesson a = lessonRepository.findById(lessonId).orElseThrow();
        if (!a.getSubject().getId().equals(subjectId) || Math.abs(delta) != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Lesson b = lessonRepository.findBySubjectIdAndLessonNumber(
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
    public List<Lesson> undated(Long subjectId) {
        return lessonRepository.findBySubjectIdOrderByLessonNumber(subjectId).stream()
                .filter(l -> l.getPlannedDate() == null).toList();
    }

    @Transactional(readOnly = true)
    public long gradeCount(Long lessonId) {
        return studentGradeRepository.countByLessonId(lessonId);
    }

    @Transactional
    public Long deleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();
        if (lesson.getPlannedDate() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        Long subjectId = lesson.getSubject().getId();
        lessonRepository.delete(lesson);
        lessonRepository.flush();
        lessonRepository.renumberStep1(subjectId);
        lessonRepository.renumberStep2(subjectId);
        return subjectId;
    }

    public record ImportResult(int assigned, List<String> extra) {
    }

    @Transactional
    public ImportResult importTopics(Long subjectId, String text) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        List<String> topics = topicListParser.parse(text);
        List<Lesson> empty = lessonRepository.findBySubjectIdOrderByLessonNumber(subjectId).stream()
                .filter(l -> "lesson".equals(l.getLessonType()))
                .filter(l -> l.getTopic() == null || l.getTopic().isBlank())
                .toList();
        int n = Math.min(topics.size(), empty.size());
        for (int i = 0; i < n; i++) {
            empty.get(i).setTopic(topics.get(i));
        }
        List<String> extra = new ArrayList<>(topics.subList(n, topics.size()));
        List<Lesson> created = new ArrayList<>();
        int temp = 900000;
        for (String topic : extra) {
            Lesson l = new Lesson();
            l.setSubject(subject);
            l.setLessonType("lesson");
            l.setTopic(topic);
            l.setLessonNumber(++temp);
            created.add(l);
        }
        lessonRepository.saveAll(created);
        if (!created.isEmpty()) {
            lessonRepository.renumberStep1(subjectId);
            lessonRepository.renumberStep2(subjectId);
        }
        return new ImportResult(n, extra);
    }

    @Transactional
    public Long clearLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();
        lesson.setTopic(null);
        lesson.setComment(null);
        lesson.setLecture(null);
        lesson.setClassworkStart(null);
        lesson.setClassworkJoint(null);
        lesson.setClassworkEnd(null);
        lesson.setHomework(null);
        return lesson.getSubject().getId();
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, String> offDays(Long subjectId) {
        List<LocalDate> dates = lessonRepository.findBySubjectIdOrderByLessonNumber(subjectId).stream()
                .map(Lesson::getPlannedDate).filter(Objects::nonNull).toList();
        if (dates.isEmpty()) {
            return Map.of();
        }
        Map<LocalDate, String> result = new HashMap<>();
        for (CalendarDay d : calendarDayRepository
                .findByCalendarDateBetween(Collections.min(dates), Collections.max(dates))) {
            if (!"working".equals(d.getDayType())) {
                result.put(d.getCalendarDate(), d.getDayType());
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<TextbookGroup> textbooksByClass(LocalDate today) {
        int year = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
        Map<String, Map<Textbook, List<TextbookEntry.Usage>>> byClass = new TreeMap<>();
        Map<Textbook, Long> firstSubject = new HashMap<>();
        for (Subject s : subjectRepository.findByAcademicYearOrderByName(year)) {
            if (s.getTextbook() == null || s.getClasses().isEmpty()) {
                continue;
            }
            String key = s.getClasses().stream()
                    .map(SchoolClass::getGrade).distinct().sorted()
                    .map(String::valueOf).collect(Collectors.joining("/"));
            String groups = s.getClasses().stream().map(SchoolClass::getClassName).sorted()
                    .collect(Collectors.joining(", "));
            String label = s.getName() + " " + groups;
            if (s.getCoverage() != null) {
                label += ": " + s.getCoverage().replace('_', ' ');
                if (s.getCoverageNote() != null) {
                    label += " (" + s.getCoverageNote() + ")";
                }
            }
            firstSubject.putIfAbsent(s.getTextbook(), s.getId());
            byClass.computeIfAbsent(key, k -> new LinkedHashMap<>())
                    .computeIfAbsent(s.getTextbook(), t -> new ArrayList<>())
                    .add(new TextbookEntry.Usage(s.getId(), label, s.getCoverage() != null,
                            s.getApprovedBy(), s.getApprovedOn()));
        }
        List<TextbookGroup> result = new ArrayList<>();
        byClass.forEach((k, v) -> {
            List<TextbookEntry> entries = new ArrayList<>();
            v.forEach((t, usage) -> entries.add(new TextbookEntry(t, firstSubject.get(t), usage)));
            result.add(new TextbookGroup(k, entries));
        });
        return result;
    }

    @Transactional
    public void saveCoverage(Long subjectId, String coverage, String note) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow();
        subject.setCoverage(coverage == null || coverage.isBlank() ? null : coverage);
        subject.setCoverageNote(note == null || note.isBlank() ? null : note.trim());
    }
}