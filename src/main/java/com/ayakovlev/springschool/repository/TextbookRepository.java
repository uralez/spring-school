package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.Textbook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextbookRepository extends JpaRepository<Textbook, Long> {
}