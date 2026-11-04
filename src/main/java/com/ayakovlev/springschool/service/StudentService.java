package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.entity.*;
import com.ayakovlev.springschool.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudyGroupRepository studyGroupRepository;
    private final StudentRepository studentRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public List<StudyGroup> groups() {
        return studyGroupRepository.findAllByOrderByClassYearGradeAscNameAsc();
    }

    @Transactional(readOnly = true)
    public StudyGroup group(Long id) {
        return studyGroupRepository.findById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<GroupMembership> members(Long groupId) {
        return groupMembershipRepository.findByGroupIdAndActiveTrueOrderByStudentUserFullName(groupId);
    }

    @Transactional
    public void addStudent(Long groupId, String fullName) {
        AppUser user = new AppUser();
        user.setUsername("student-" + UUID.randomUUID());
        user.setFullName(fullName.trim());
        appUserRepository.save(user);

        Student student = new Student();
        student.setUser(user);
        studentRepository.save(student);

        GroupMembership membership = new GroupMembership();
        membership.setStudent(student);
        membership.setGroup(studyGroupRepository.findById(groupId).orElseThrow());
        membership.setActive(true);
        groupMembershipRepository.save(membership);
    }
}