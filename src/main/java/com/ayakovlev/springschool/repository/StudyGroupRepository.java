package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    List<StudyGroup> findAllByOrderByClassYearGradeAscNameAsc();
}