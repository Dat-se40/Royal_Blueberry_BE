package com.example.Royal_Blueberry.repository;

import com.example.Royal_Blueberry.util.AuthProvider;
import com.example.Royal_Blueberry.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByProviderAndGoogleId(AuthProvider provider, String googleId);

    boolean existsByEmailIgnoreCase(String email);
}
