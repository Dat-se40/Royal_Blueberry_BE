package com.example.Royal_Blueberry.entity;

import com.example.Royal_Blueberry.util.AuthProvider;
import com.example.Royal_Blueberry.util.Role;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String password;

    @Indexed(unique = true, sparse = true)
    private String email;

    private String displayName;

    private String avatarUrl;

    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    private String googleId;

    @Builder.Default
    private Role role = Role.USER;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
