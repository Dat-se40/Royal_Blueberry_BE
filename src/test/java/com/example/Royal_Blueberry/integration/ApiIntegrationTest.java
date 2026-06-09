package com.example.Royal_Blueberry.integration;

import com.example.Royal_Blueberry.dto.PackageDto;
import com.example.Royal_Blueberry.dto.WordDetailDto;
import com.example.Royal_Blueberry.dto.auth.AuthResponse;
import com.example.Royal_Blueberry.dto.auth.RegisterRequest;
import com.example.Royal_Blueberry.dto.auth.UserInfo;
import com.example.Royal_Blueberry.entity.Tag;
import com.example.Royal_Blueberry.entity.User;
import com.example.Royal_Blueberry.repository.PackageDetailRepository;
import com.example.Royal_Blueberry.repository.PackageRepository;
import com.example.Royal_Blueberry.repository.TagRepository;
import com.example.Royal_Blueberry.repository.UserRepository;
import com.example.Royal_Blueberry.repository.WordTagRelationRepository;
import com.example.Royal_Blueberry.security.CustomUserDetails;
import com.example.Royal_Blueberry.security.CustomUserDetailsService;
import com.example.Royal_Blueberry.security.JwtTokenProvider;
import com.example.Royal_Blueberry.service.AuthService;
import com.example.Royal_Blueberry.service.FindWordService;
import com.example.Royal_Blueberry.service.PackageDetailService;
import com.example.Royal_Blueberry.service.PackageService;
import com.example.Royal_Blueberry.service.impl.TagService;
import com.example.Royal_Blueberry.util.Role;
import com.example.Royal_Blueberry.util.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.access_key=YWNjZXNzLXNlY3JldC1rZXktYWNjZXNzLXNlY3JldC1rZXktMTIzNA==",
        "jwt.refresh_key=cmVmcmVzaC1zZWNyZXQta2V5LXJlZnJlc2gtc2VjcmV0LTEyMzQ=",
        "jwt.access-expiration=30",
        "jwt.refresh-expiration=60",
        "merriam.webster.dict-key=test",
        "merriam.webster.thesaurus-key=test",
        "merriam.webster.dict-uri=https://example.test/dict",
        "merriam.webster.thesaurus-uri=https://example.test/thesaurus",
        "free.dictionary.uri=https://example.test/free/",
        "google.client-id=test-client",
        "google.client-secret=test-secret",
        "google.redirect-uri=http://localhost/callback"
})
@AutoConfigureMockMvc
@ActiveProfiles("no_ai")
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private FindWordService findWordService;

    @MockBean
    private PackageService packageService;

    @MockBean
    private PackageDetailService packageDetailService;

    @MockBean
    private TagService tagService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean(name = "mongoMappingContext")
    private MongoMappingContext mongoMappingContext;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PackageRepository packageRepository;

    @MockBean
    private PackageDetailRepository packageDetailRepository;

    @MockBean
    private TagRepository tagRepository;

    @MockBean
    private WordTagRelationRepository wordTagRelationRepository;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new CustomUserDetails(User.builder()
                .id("u1")
                .email("user@example.com")
                .displayName("User")
                .role(Role.USER)
                .build());
    }

    @Test
    void publicDictionaryEndpointIsAccessibleWithoutAuthentication() throws Exception {
        when(findWordService.findWord("hello")).thenReturn(WordDetailDto.builder()
                .word("hello")
                .phonetic("/huh-loh/")
                .meanings(List.of())
                .build());

        mockMvc.perform(get("/api/searching/get-detail/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.word").value("hello"))
                .andExpect(jsonPath("$.phonetic").value("/huh-loh/"));
    }

    @Test
    void protectedPackagesEndpointRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(post("/api/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Business","category":"vocabulary","level":"advanced"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedPackagesEndpointAcceptsValidBearerToken() throws Exception {
        stubAccessAuthentication();
        when(packageService.createPackage(any(PackageDto.class))).thenReturn(
                new PackageDto(
                        "pkg-1",
                        "Business",
                        "vocabulary",
                        "advanced",
                        "Business terms",
                        0,
                        LocalDateTime.of(2026, 6, 9, 9, 0)
                )
        );

        mockMvc.perform(post("/api/packages")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Business","category":"vocabulary","level":"advanced","description":"Business terms","totalWords":0}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("pkg-1"))
                .andExpect(jsonPath("$.name").value("Business"));
    }

    @Test
    void authValidationErrorsGoThroughGlobalExceptionHandler() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bad-email","password":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.email").value("Email must be valid"))
                .andExpect(jsonPath("$.errors.password").value("Password must be between 6 and 100 characters"));
    }

    @Test
    void authenticatedMeEndpointUsesPrincipalResolvedByJwtFilter() throws Exception {
        stubAccessAuthentication();
        when(authService.getCurrentUser("u1")).thenReturn(UserInfo.builder()
                .id("u1")
                .email("user@example.com")
                .displayName("User")
                .role("USER")
                .build());

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("u1"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void authenticatedTagsEndpointPassesUserIdentityToService() throws Exception {
        stubAccessAuthentication();
        when(tagService.getAllTagsByUser("u1")).thenReturn(List.of(
                new Tag("tag-1", "u1", "Favorites", "star", "yellow", null)
        ));

        mockMvc.perform(get("/api/tags")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("tag-1"))
                .andExpect(jsonPath("$[0].userId").value("u1"));

        verify(tagService).getAllTagsByUser("u1");
    }

    private void stubAccessAuthentication() {
        when(jwtTokenProvider.isRefreshToken("access-token")).thenReturn(false);
        when(jwtTokenProvider.validateToken("access-token", TokenType.ACCESS)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("access-token", TokenType.ACCESS)).thenReturn("u1");
        when(userDetailsService.loadUserById("u1")).thenReturn(userDetails);
    }
}
