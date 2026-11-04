package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.entity.AppUser;
import com.ayakovlev.springschool.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        if (user.getPasswordHash() == null) {
            throw new UsernameNotFoundException(username);
        }
        return new User(user.getUsername(), user.getPasswordHash(),
                user.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList());
    }
}