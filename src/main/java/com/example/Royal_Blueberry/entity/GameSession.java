package com.example.Royal_Blueberry.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Game Sessions")
@CompoundIndex(name = "user_startTime_idx", def = "{'userId': 1, 'startTime': -1}")
public class GameSession {

    @Id
    private String id;

    @Indexed
    private String userId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /** "All" hoặc tagId dạng string */
    private String dataSource;
    private String dataSourceName;

    private int totalCards;
    private int knownCards;
    private int unknownCards;
    private double accuracyPercentage;

    /** Thời lượng tính bằng giây */
    private long durationSeconds;

    private List<Integer> skippedCardIndices = new ArrayList<>();
    private List<String> skippedWords = new ArrayList<>();

    private LocalDateTime createdAt;
}
