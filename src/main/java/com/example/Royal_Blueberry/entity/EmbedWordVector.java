package com.example.Royal_Blueberry.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Document(collection = "Embed Word Vectors")
@Data
@Builder
public class EmbedWordVector {
    @Id
    private String word ;
    private String definition ;
    private List<Double> vector; // semantic vector 384 chiều, được lấy từ model
    private LocalDateTime createAt ;

    //Probing Neural Network Comprehension of Natural Language Arguments

    private float sentimentScore;  // -1.0 → +1.0
    private float formalityScore;  // -1.0 → +1.0
    private float emotionDomain;   // 0.0  → +1.0
    private float personDomain;    // 0.0  → +1.0
    private float actionDomain;    // 0.0  → +1.0
}
