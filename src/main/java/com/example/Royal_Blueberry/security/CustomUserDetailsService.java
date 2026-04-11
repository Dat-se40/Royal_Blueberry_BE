package com.example.Royal_Blueberry.security;

import com.example.Royal_Blueberry.entity.User;
import com.example.Royal_Blueberry.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("[UserDetailsService] Loading user by email: {}", username);
        User user = userRepository.findByEmailIgnoreCase(normalizeEmail(username))
                .orElseThrow(() -> {
                    log.warn("[UserDetailsService] User not found with email: {}", username);
                    return new UsernameNotFoundException(
                            "User not found with email: " + username);
                });
        log.debug("[UserDetailsService] User loaded - userId={}", user.getId());
        return new CustomUserDetails(user);
    }

    public UserDetails loadUserById(String id) {
        log.debug("[UserDetailsService] Loading user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[UserDetailsService] User not found with id: {}", id);
                    return new UsernameNotFoundException(
                            "User not found with id: " + id);
                });
        return new CustomUserDetails(user);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
