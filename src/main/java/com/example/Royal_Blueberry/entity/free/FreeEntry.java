package com.example.Royal_Blueberry.entity.free;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

// Chứa header của 1 từ
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FreeEntry {
    private String word;
    private String phonetic;
    private List<FreePhonetic> phonetics;
    private List<FreeMeaning> meanings;
}