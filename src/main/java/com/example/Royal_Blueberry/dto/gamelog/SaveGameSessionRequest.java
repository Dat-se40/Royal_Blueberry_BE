package com.example.Royal_Blueberry.dto.gamelog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveGameSessionRequest {

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotBlank
    private String dataSource;

    @NotBlank
    private String dataSourceName;

    @Min(0)
    private int totalCards;

    @Min(0)
    private int knownCards;

    @Min(0)
    private int unknownCards;

    private double accuracyPercentage;

    @Min(0)
    private long durationSeconds;

    private List<Integer> skippedCardIndices;
    private List<String> skippedWords;
}
