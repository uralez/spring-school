package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.repository.CourseLessonRepository;
import com.ayakovlev.springschool.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service("access")
@RequiredArgsConstructor
public class AccessService {

    private static final Set<String> EDIT_PLAN = Set.of("ADMIN", "TEACHER", "METHODIST");
    private static final Set<String> APPROVE = Set.of("ADMIN", "DIRECTOR", "DEPUTY", "METHODIST");

    private final SubjectRepository subjectRepository;
    private final CourseLessonRepository courseLessonRepository;

    public boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "ADMIN");
    }

    public boolean canEditPlans(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> EDIT_PLAN.contains(a.getAuthority().replace("ROLE_", "")));
    }

    @Transactional(readOnly = true)
    public boolean canEditSubject(Authentication authentication, Long subjectId) {
        if (authentication == null) {
            return false;
        }
        if (isAdmin(authentication) || hasRole(authentication, "METHODIST")) {
            return true;
        }
        if (!hasRole(authentication, "TEACHER")) {
            return false;
        }
        return subjectRepository.findById(subjectId)
                .map(s -> s.getTeachers().stream()
                        .anyMatch(t -> t.getUser().getUsername().equals(authentication.getName())))
                .orElse(false);
    }

    public boolean canApprove(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> APPROVE.contains(a.getAuthority().replace("ROLE_", "")));
    }

    @Transactional(readOnly = true)
    public boolean canEditLesson(Authentication authentication, Long lessonId) {
        return courseLessonRepository.findById(lessonId)
                .map(l -> canEditSubject(authentication, l.getSubject().getId()))
                .orElse(false);
    }
}