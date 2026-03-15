package com.example.Royal_Blueberry.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordDetailDto {

    private String word;
    private String phonetic;        // ưu tiên Free (đẹp hơn: /həˈloʊ/)
    private String audioUs;         // từ Free phonetics
    private String audioUk;         // từ Free phonetics
    private String imageUrl;        // từ Pixabay/SerpAPI sau này
    private List<MeaningDto> meanings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeaningDto {
        private int meaningIndex;       // thứ tự trong list
        private String partOfSpeech;    // "noun", "verb"...
        private List<DefinitionDto> definitions;
        private List<String> synonyms;  // level meaning (từ MW thesaurus)
        private List<String> antonyms;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefinitionDto {
        private int definitionIndex;
        private String definition;
        private String example;         // Free có, MW shortdef không có
        private List<String> synonyms;  // level definition (từ Free)
        private List<String> antonyms;
    }
}