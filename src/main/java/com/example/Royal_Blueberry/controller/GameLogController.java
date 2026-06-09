package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.gamelog.GameLogSummaryResponse;
import com.example.Royal_Blueberry.dto.gamelog.SaveGameSessionRequest;
import com.example.Royal_Blueberry.entity.GameSession;
import com.example.Royal_Blueberry.security.CustomUserDetails;
import com.example.Royal_Blueberry.service.GameLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/game-logs")
@RequiredArgsConstructor
@Slf4j
public class GameLogController {

    private final GameLogService gameLogService;

    @PostMapping("/sessions")
    public ResponseEntity<GameSession> saveSession(
            Principal principal,
            @Valid @RequestBody SaveGameSessionRequest request) {
        String userId = getUserId(principal);
        log.info("[GameLog] POST /sessions - userId={}", userId);
        GameSession session = gameLogService.saveSession(userId, request);
        return new ResponseEntity<>(session, HttpStatus.CREATED);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<GameSession>> getRecentSessions(
            Principal principal,
            @RequestParam(defaultValue = "20") int limit) {
        String userId = getUserId(principal);
        log.info("[GameLog] GET /sessions - userId={}, limit={}", userId, limit);
        return ResponseEntity.ok(gameLogService.getRecentSessions(userId, limit));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<GameSession> getSession(
            Principal principal,
            @PathVariable String id) {
        String userId = getUserId(principal);
        log.info("[GameLog] GET /sessions/{} - userId={}", id, userId);
        return ResponseEntity.ok(gameLogService.getSession(userId, id));
    }

    @GetMapping("/summary")
    public ResponseEntity<GameLogSummaryResponse> getSummary(Principal principal) {
        String userId = getUserId(principal);
        log.info("[GameLog] GET /summary - userId={}", userId);
        return ResponseEntity.ok(gameLogService.getSummary(userId));
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<Void> clearAllSessions(Principal principal) {
        String userId = getUserId(principal);
        log.info("[GameLog] DELETE /sessions - userId={}", userId);
        gameLogService.clearAllSessions(userId);
        return ResponseEntity.noContent().build();
    }

    private String getUserId(Principal principal) {
        UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) principal;
        CustomUserDetails userDetails = (CustomUserDetails) authToken.getPrincipal();
        return userDetails.getUser().getId();
    }
}
