package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {

    @Query("""
            SELECT l FROM Lesson l
            LEFT JOIN FETCH l.groups
            WHERE l.validFrom <= :date
              AND (l.validTo IS NULL OR l.validTo >= :date)
            ORDER BY l.dayOfWeek, l.startTime
            """)
    List<Lesson> findActiveOn(@Param("date") LocalDate date);
}