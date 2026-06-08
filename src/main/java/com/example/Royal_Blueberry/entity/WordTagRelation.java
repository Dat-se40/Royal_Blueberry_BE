package com.example.Royal_Blueberry.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Word Tag Relations")
public class WordTagRelation {
    @Id
    private String id;
    private String userId;
    private String word;
    private Integer meaningIndex;
    private String tagId;
    private Boolean isFavourite ;
    private String note  ;
    private Instant linkedAt;
}