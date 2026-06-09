package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.dto.gamelog.GameLogSummaryResponse;
import com.example.Royal_Blueberry.dto.gamelog.SaveGameSessionRequest;
import com.example.Royal_Blueberry.entity.GameSession;
import com.example.Royal_Blueberry.exception.AuthException;
import com.example.Royal_Blueberry.repository.GameSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameLogServiceImplTest {

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private GameLogServiceImpl gameLogService;

    @Test
    void saveSessionPersistsUserScopedSession() {
        SaveGameSessionRequest request = buildRequest();
        when(gameSessionRepository.save(any(GameSession.class))).thenAnswer(invocation -> {
            GameSession session = invocation.getArgument(0);
            session.setId("session-1");
            return session;
        });

        GameSession saved = gameLogService.saveSession("u1", request);

        ArgumentCaptor<GameSession> captor = ArgumentCaptor.forClass(GameSession.class);
        verify(gameSessionRepository).save(captor.capture());

        GameSession session = captor.getValue();
        assertEquals("u1", session.getUserId());
        assertEquals("All", session.getDataSource());
        assertEquals(10, session.getTotalCards());
        assertEquals(List.of(2, 7), session.getSkippedCardIndices());
        assertEquals(List.of("abandon", "zealous"), session.getSkippedWords());
        assertNotNull(session.getCreatedAt());
        assertEquals("session-1", saved.getId());
    }

    @Test
    void saveSessionRejectsEndTimeBeforeStartTime() {
        SaveGameSessionRequest request = buildRequest();
        request.setEndTime(request.getStartTime().minusMinutes(1));

        AuthException ex = assertThrows(AuthException.class,
                () -> gameLogService.saveSession("u1", request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void getRecentSessionsCapsLimit() {
        when(gameSessionRepository.findByUserIdOrderByStartTimeDesc(eq("u1"), any(Pageable.class)))
                .thenReturn(List.of(new GameSession()));

        List<GameSession> sessions = gameLogService.getRecentSessions("u1", 500);

        assertEquals(1, sessions.size());
        verify(gameSessionRepository).findByUserIdOrderByStartTimeDesc(eq("u1"), any(Pageable.class));
    }

    @Test
    void getSessionReturnsNotFoundForMissingSession() {
        when(gameSessionRepository.findById("missing")).thenReturn(Optional.empty());

        AuthException ex = assertThrows(AuthException.class,
                () -> gameLogService.getSession("u1", "missing"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void getSessionRejectsOtherUsersSession() {
        GameSession session = new GameSession();
        session.setId("session-1");
        session.setUserId("other-user");
        when(gameSessionRepository.findById("session-1")).thenReturn(Optional.of(session));

        AuthException ex = assertThrows(AuthException.class,
                () -> gameLogService.getSession("u1", "session-1"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void getSummaryReturnsZerosWhenNoSessions() {
        AggregationResults<GameLogServiceImpl.SummaryAggregation> results =
                org.mockito.Mockito.mock(AggregationResults.class);
        when(results.getUniqueMappedResult()).thenReturn(null);
        when(mongoTemplate.aggregate(any(), eq("Game Sessions"), eq(GameLogServiceImpl.SummaryAggregation.class)))
                .thenReturn(results);

        GameLogSummaryResponse summary = gameLogService.getSummary("u1");

        assertEquals(0, summary.getTotalGamesPlayed());
        assertEquals(0, summary.getTotalCardsStudied());
        assertEquals(0, summary.getAverageAccuracy());
        assertEquals(0, summary.getTotalStudyTimeSeconds());
    }

    @Test
    void clearAllSessionsDeletesByUserId() {
        gameLogService.clearAllSessions("u1");
        verify(gameSessionRepository).deleteByUserId("u1");
    }

    private SaveGameSessionRequest buildRequest() {
        SaveGameSessionRequest request = new SaveGameSessionRequest();
        request.setStartTime(LocalDateTime.of(2026, 6, 9, 14, 30));
        request.setEndTime(LocalDateTime.of(2026, 6, 9, 14, 35, 42));
        request.setDataSource("All");
        request.setDataSourceName("All Words");
        request.setTotalCards(10);
        request.setKnownCards(8);
        request.setUnknownCards(2);
        request.setAccuracyPercentage(80.0);
        request.setDurationSeconds(342);
        request.setSkippedCardIndices(List.of(2, 7));
        request.setSkippedWords(List.of("abandon", "zealous"));
        return request;
    }
}
