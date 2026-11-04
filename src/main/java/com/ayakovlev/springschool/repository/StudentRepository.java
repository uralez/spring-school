package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}