package com.ayakovlev.springschool.repository;

import com.ayakovlev.springschool.entity.GroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {

    List<GroupMembership> findByGroupIdAndActiveTrueOrderByStudentUserFullName(Long groupId);
}