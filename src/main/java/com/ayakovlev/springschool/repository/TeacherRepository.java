package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByUserUsername(String username);
}