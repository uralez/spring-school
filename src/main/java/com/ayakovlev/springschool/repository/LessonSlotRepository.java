package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.LessonSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonSlotRepository extends JpaRepository<LessonSlot, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM LessonSlot s WHERE s.subject.id = :subjectId")
    void deleteBySubject(@Param("subjectId") Long subjectId);
}