package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    @Query("""
            SELECT DISTINCT s FROM Subject s JOIN s.classes c
            WHERE s.name = :name AND s.academicYear = :year AND c.className = :className
            """)
    Optional<Subject> findByNameYearAndClass(@Param("name") String name,
                                             @Param("year") int year,
                                             @Param("className") String className);

    List<Subject> findByAcademicYear(Integer academicYear);
}