package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.gamelog.GameLogSummaryResponse;
import com.example.Royal_Blueberry.dto.gamelog.SaveGameSessionRequest;
import com.example.Royal_Blueberry.entity.GameSession;
import com.example.Royal_Blueberry.entity.User;
import com.example.Royal_Blueberry.security.CustomUserDetails;
import com.example.Royal_Blueberry.service.GameLogService;
import com.example.Royal_Blueberry.util.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameLogControllerTest {

    @Mock
    private GameLogService gameLogService;

    @InjectMocks
    private GameLogController controller;

    @Test
    void saveSessionReturnsCreated() {
        SaveGameSessionRequest request = new SaveGameSessionRequest();
        GameSession session = new GameSession();
        session.setId("session-1");
        when(gameLogService.saveSession("u1", request)).thenReturn(session);

        ResponseEntity<GameSession> response = controller.saveSession(principal(), request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("session-1", response.getBody().getId());
    }

    @Test
    void getRecentSessionsUsesAuthenticatedUser() {
        when(gameLogService.getRecentSessions("u1", 20))
                .thenReturn(List.of(new GameSession()));

        ResponseEntity<List<GameSession>> response = controller.getRecentSessions(principal(), 20);

        assertEquals(1, response.getBody().size());
        verify(gameLogService).getRecentSessions("u1", 20);
    }

    @Test
    void getSessionDelegatesToService() {
        GameSession session = new GameSession();
        session.setId("session-1");
        when(gameLogService.getSession("u1", "session-1")).thenReturn(session);

        ResponseEntity<GameSession> response = controller.getSession(principal(), "session-1");

        assertEquals("session-1", response.getBody().getId());
    }

    @Test
    void getSummaryReturnsAggregatedStats() {
        when(gameLogService.getSummary("u1"))
                .thenReturn(new GameLogSummaryResponse(15, 150, 76.4, 5120));

        ResponseEntity<GameLogSummaryResponse> response = controller.getSummary(principal());

        assertEquals(15, response.getBody().getTotalGamesPlayed());
        assertEquals(76.4, response.getBody().getAverageAccuracy());
    }

    @Test
    void clearAllSessionsReturnsNoContent() {
        ResponseEntity<Void> response = controller.clearAllSessions(principal());

        assertEquals(204, response.getStatusCode().value());
        verify(gameLogService).clearAllSessions("u1");
    }

    private Principal principal() {
        CustomUserDetails userDetails = new CustomUserDetails(User.builder()
                .id("u1")
                .email("user@example.com")
                .role(Role.USER)
                .build());
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
