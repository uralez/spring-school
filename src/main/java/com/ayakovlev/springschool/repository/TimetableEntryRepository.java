package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Integer> {

    @Query("""
            SELECT l FROM TimetableEntry l
            LEFT JOIN FETCH l.groups
            WHERE l.validFrom <= :date
              AND (l.validTo IS NULL OR l.validTo >= :date)
            ORDER BY l.dayOfWeek, l.startTime
            """)
    List<TimetableEntry> findActiveOn(@Param("date") LocalDate date);

    // method ir replaced with findBySubjectIdOrderByStartTime
//    @Query("""
//        SELECT DISTINCT l FROM Lesson l JOIN l.groups g
//        WHERE l.subjectName = :name AND g IN :groups
//        ORDER BY l.startTime
//        """)
//    List<Lesson> findBySubjectAndGroups(@Param("name") String name, @Param("groups") Collection<String> groups);

    List<TimetableEntry> findBySubjectIdOrderByStartTime(Long subjectId);
}