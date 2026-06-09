package com.example.Royal_Blueberry.mapper;

import com.example.Royal_Blueberry.dto.PackageDetailDto;
import com.example.Royal_Blueberry.dto.WordEntryDto;
import com.example.Royal_Blueberry.entity.PackageDetail;
import com.example.Royal_Blueberry.entity.WordEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageDetailMapperTest {

    @Test
    void toDtoMapsWordsAndCountsThem() {
        WordEntry word = new WordEntry();
        word.setWord("hello");
        word.setPhonetic("/he-lo/");
        word.setPartOfSpeech("interjection");
        word.setDefinition("used as a greeting");
        word.setExample("Hello there");

        PackageDetail detail = PackageDetail.builder()
                .id("detail-1")
                .packageId("pkg-1")
                .words(List.of(word))
                .build();

        PackageDetailDto dto = PackageDetailMapper.toDto(detail);

        assertEquals("detail-1", dto.getId());
        assertEquals("pkg-1", dto.getPackageId());
        assertEquals(1, dto.getTotalWords());
        assertEquals(1, dto.getWords().size());
        assertEquals("hello", dto.getWords().get(0).getWord());
        assertEquals("/he-lo/", dto.getWords().get(0).getPhonetic());
    }

    @Test
    void toDtoReturnsEmptyWordListWhenEntityWordsAreNull() {
        PackageDetail detail = PackageDetail.builder()
                .id("detail-2")
                .packageId("pkg-2")
                .words(null)
                .build();

        PackageDetailDto dto = PackageDetailMapper.toDto(detail);

        assertNotNull(dto.getWords());
        assertTrue(dto.getWords().isEmpty());
        assertEquals(0, dto.getTotalWords());
    }

    @Test
    void toEntityMapsWords() {
        WordEntryDto word = WordEntryDto.builder()
                .word("world")
                .phonetic("/wurld/")
                .partOfSpeech("noun")
                .definition("earth")
                .example("around the world")
                .build();

        PackageDetailDto dto = PackageDetailDto.builder()
                .id("detail-3")
                .packageId("pkg-3")
                .words(List.of(word))
                .build();

        PackageDetail entity = PackageDetailMapper.toEntity(dto);

        assertEquals("detail-3", entity.getId());
        assertEquals("pkg-3", entity.getPackageId());
        assertEquals(1, entity.getWords().size());
        assertEquals("world", entity.getWords().get(0).getWord());
        assertEquals("earth", entity.getWords().get(0).getDefinition());
    }

    @Test
    void toEntityReturnsEmptyWordListWhenDtoWordsAreNull() {
        PackageDetailDto dto = PackageDetailDto.builder()
                .id("detail-4")
                .packageId("pkg-4")
                .words(null)
                .build();

        PackageDetail entity = PackageDetailMapper.toEntity(dto);

        assertNotNull(entity.getWords());
        assertTrue(entity.getWords().isEmpty());
    }
}
