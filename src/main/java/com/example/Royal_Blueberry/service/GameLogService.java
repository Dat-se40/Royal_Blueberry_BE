package com.example.Royal_Blueberry.service;

import com.example.Royal_Blueberry.dto.gamelog.GameLogSummaryResponse;
import com.example.Royal_Blueberry.dto.gamelog.SaveGameSessionRequest;
import com.example.Royal_Blueberry.entity.GameSession;

import java.util.List;

public interface GameLogService {

    GameSession saveSession(String userId, SaveGameSessionRequest request);

    List<GameSession> getRecentSessions(String userId, int limit);

    GameSession getSession(String userId, String sessionId);

    GameLogSummaryResponse getSummary(String userId);

    void clearAllSessions(String userId);
}
