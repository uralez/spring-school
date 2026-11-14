package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.LessonSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonSlotRepository extends JpaRepository<LessonSlot, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM LessonSlot s WHERE s.subject.id = :subjectId")
    void deleteBySubject(@Param("subjectId") Long subjectId);

    List<LessonSlot> findBySubjectIdOrderBySlotNumber(Long subjectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE lesson_slot s SET slot_number = r.n + 100000
        FROM (SELECT id, row_number() OVER (ORDER BY slot_date) AS n
              FROM lesson_slot WHERE subject_id = :subjectId) r
        WHERE s.id = r.id
        """, nativeQuery = true)
    void renumberStep1(@Param("subjectId") Long subjectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE lesson_slot SET slot_number = slot_number - 100000 WHERE subject_id = :subjectId",
            nativeQuery = true)
    void renumberStep2(@Param("subjectId") Long subjectId);
}