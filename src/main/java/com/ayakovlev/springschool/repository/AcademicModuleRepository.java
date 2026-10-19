package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.AcademicModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface AcademicModuleRepository extends JpaRepository<AcademicModule, Integer> {

    @Query("SELECT m FROM AcademicModule m WHERE m.startDate <= :date AND m.endDate >= :date")
    Optional<AcademicModule> findByDate(@Param("date") LocalDate date);
}