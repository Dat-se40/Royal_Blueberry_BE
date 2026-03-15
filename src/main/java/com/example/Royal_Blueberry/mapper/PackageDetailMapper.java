package com.example.Royal_Blueberry.mapper;

import com.example.Royal_Blueberry.dto.PackageDetailDto;
import com.example.Royal_Blueberry.dto.WordEntryDto;
import com.example.Royal_Blueberry.entity.PackageDetail;
import com.example.Royal_Blueberry.entity.WordEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PackageDetailMapper {

    public static PackageDetailDto toDto(PackageDetail detail) {
        List<WordEntryDto> wordDtos = detail.getWords() == null
                ? new ArrayList<>()
                : detail.getWords().stream()
                .map(PackageDetailMapper::toWordDto)
                .collect(Collectors.toList());

        return PackageDetailDto.builder()
                .id(detail.getId())
                .packageId(detail.getPackageId())
                .words(wordDtos)
                .totalWords(wordDtos.size())
                .build();
    }

    public static PackageDetail toEntity(PackageDetailDto dto) {
        List<WordEntry> words = dto.getWords() == null
                ? new ArrayList<>()
                : dto.getWords().stream()
                .map(PackageDetailMapper::toWordEntity)
                .collect(Collectors.toList());

        return PackageDetail.builder()
                .id(dto.getId())
                .packageId(dto.getPackageId())
                .words(words)
                .build();
    }

    public static WordEntryDto toWordDto(WordEntry w) {
        return WordEntryDto.builder()
                .word(w.getWord())
                .phonetic(w.getPhonetic())
                .partOfSpeech(w.getPartOfSpeech())
                .definition(w.getDefinition())
                .example(w.getExample())
                .build();
    }

    public static WordEntry toWordEntity(WordEntryDto dto) {
        WordEntry w = new WordEntry();
        w.setWord(dto.getWord());
        w.setPhonetic(dto.getPhonetic());
        w.setPartOfSpeech(dto.getPartOfSpeech());
        w.setDefinition(dto.getDefinition());
        w.setExample(dto.getExample());
        return w;
    }
}