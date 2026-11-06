package com.ayakovlev.springschool.config;

import com.ayakovlev.springschool.service.AccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class CurrentUserAdvice {
    private final AccessService access;

    @ModelAttribute("currentUser")
    public String currentUser(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication.getName();
    }

    @ModelAttribute("menuAdmin")
    public boolean menuAdmin(Authentication authentication) {
        return access.isAdmin(authentication);
    }
}