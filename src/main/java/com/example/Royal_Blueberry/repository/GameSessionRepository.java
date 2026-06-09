package com.example.Royal_Blueberry.repository;

import com.example.Royal_Blueberry.entity.GameSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameSessionRepository extends MongoRepository<GameSession, String> {

    List<GameSession> findByUserIdOrderByStartTimeDesc(String userId, Pageable pageable);

    long countByUserId(String userId);

    void deleteByUserId(String userId);
}
