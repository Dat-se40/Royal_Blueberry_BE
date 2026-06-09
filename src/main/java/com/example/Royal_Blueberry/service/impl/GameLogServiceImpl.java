package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.dto.gamelog.GameLogSummaryResponse;
import com.example.Royal_Blueberry.dto.gamelog.SaveGameSessionRequest;
import com.example.Royal_Blueberry.entity.GameSession;
import com.example.Royal_Blueberry.exception.AuthException;
import com.example.Royal_Blueberry.repository.GameSessionRepository;
import com.example.Royal_Blueberry.service.GameLogService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameLogServiceImpl implements GameLogService {

    private final GameSessionRepository gameSessionRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public GameSession saveSession(String userId, SaveGameSessionRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new AuthException("endTime must not be before startTime", HttpStatus.BAD_REQUEST);
        }

        GameSession session = new GameSession();
        session.setUserId(userId);
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setDataSource(request.getDataSource());
        session.setDataSourceName(request.getDataSourceName());
        session.setTotalCards(request.getTotalCards());
        session.setKnownCards(request.getKnownCards());
        session.setUnknownCards(request.getUnknownCards());
        session.setAccuracyPercentage(request.getAccuracyPercentage());
        session.setDurationSeconds(request.getDurationSeconds());
        session.setSkippedCardIndices(
                request.getSkippedCardIndices() != null
                        ? request.getSkippedCardIndices()
                        : new ArrayList<>()
        );
        session.setSkippedWords(
                request.getSkippedWords() != null
                        ? request.getSkippedWords()
                        : new ArrayList<>()
        );
        session.setCreatedAt(LocalDateTime.now());
        return gameSessionRepository.save(session);
    }

    @Override
    public List<GameSession> getRecentSessions(String userId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return gameSessionRepository.findByUserIdOrderByStartTimeDesc(
                userId, PageRequest.of(0, safeLimit));
    }

    @Override
    public GameSession getSession(String userId, String sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AuthException("Game session not found", HttpStatus.NOT_FOUND));

        if (!userId.equals(session.getUserId())) {
            throw new AuthException("Access denied", HttpStatus.FORBIDDEN);
        }
        return session;
    }

    @Override
    public GameLogSummaryResponse getSummary(String userId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").is(userId)),
                Aggregation.group()
                        .count().as("totalGamesPlayed")
                        .sum("totalCards").as("totalCardsStudied")
                        .avg("accuracyPercentage").as("averageAccuracy")
                        .sum("durationSeconds").as("totalStudyTimeSeconds")
        );

        AggregationResults<SummaryAggregation> results = mongoTemplate.aggregate(
                aggregation, "Game Sessions", SummaryAggregation.class);

        SummaryAggregation summary = results.getUniqueMappedResult();
        if (summary == null) {
            return new GameLogSummaryResponse(0, 0, 0, 0);
        }

        return new GameLogSummaryResponse(
                summary.getTotalGamesPlayed(),
                summary.getTotalCardsStudied(),
                summary.getAverageAccuracy(),
                summary.getTotalStudyTimeSeconds()
        );
    }

    @Override
    public void clearAllSessions(String userId) {
        gameSessionRepository.deleteByUserId(userId);
    }

    @Getter
    @Setter
    static class SummaryAggregation {
        private int totalGamesPlayed;
        private int totalCardsStudied;
        private double averageAccuracy;
        private long totalStudyTimeSeconds;
    }
}
