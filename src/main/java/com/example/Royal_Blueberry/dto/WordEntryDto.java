package com.example.Royal_Blueberry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordEntryDto {
    private String word;
    private String phonetic;
    private String partOfSpeech;
    private String definition;
    private String example;
}