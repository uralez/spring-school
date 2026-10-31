package com.ayakovlev.springschool.config;

import com.ayakovlev.springschool.entity.AppUser;
import com.ayakovlev.springschool.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USER:}")
    private String adminUser;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (adminUser.isBlank() || adminPassword.isBlank()) {
            return;
        }
        if (appUserRepository.findByUsername(adminUser).isPresent()) {
            return;
        }
        AppUser user = new AppUser();
        user.setUsername(adminUser);
        user.setPasswordHash(passwordEncoder.encode(adminPassword));
        user.getRoles().add("ADMIN");
        appUserRepository.save(user);
    }
}