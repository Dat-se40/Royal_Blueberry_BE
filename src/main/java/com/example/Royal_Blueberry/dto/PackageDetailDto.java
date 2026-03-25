package com.example.Royal_Blueberry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageDetailDto {
    private String id;
    private String packageId;
    private int totalWords;
    private List<WordEntryDto> words;

}