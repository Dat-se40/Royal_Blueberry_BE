package com.example.Royal_Blueberry.dto.gamelog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameLogSummaryResponse {

    private int totalGamesPlayed;
    private int totalCardsStudied;
    private double averageAccuracy;
    private long totalStudyTimeSeconds;
}
