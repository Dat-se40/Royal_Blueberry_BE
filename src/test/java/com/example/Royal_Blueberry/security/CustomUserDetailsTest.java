package com.example.Royal_Blueberry.security;

import com.example.Royal_Blueberry.entity.User;
import com.example.Royal_Blueberry.util.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomUserDetailsTest {

    @Test
    void exposesExpectedSpringSecurityFields() {
        User user = User.builder()
                .id("u1")
                .email("user@example.com")
                .password("secret")
                .role(Role.ADMIN)
                .build();

        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals("user@example.com", details.getUsername());
        assertEquals("secret", details.getPassword());
        assertEquals("ROLE_ADMIN", details.getAuthorities().iterator().next().getAuthority());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
    }
}
