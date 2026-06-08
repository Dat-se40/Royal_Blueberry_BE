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
@Document(collection = "Tags")
public class Tag {
    @Id
    private String id;
    private String userId;
    private String name;
    private String icon;
    private String color;
    private Instant lastModifiedAt;
}