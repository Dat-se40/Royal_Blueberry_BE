package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.dto.WordDetailDto;
import com.example.Royal_Blueberry.entity.EmbedWordVector;
import com.example.Royal_Blueberry.repository.EmbedWordVectorRepository;
import com.example.Royal_Blueberry.service.FindWordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbedWordServiceImplTest {

    @Mock
    private EmbedWordVectorRepository embedWordVectorRepository;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private FindWordService findWordService;

    @InjectMocks
    private EmbedWordServiceImpl service;

    @Test
    void loadCachesFromRepoLoadsVectorsIntoMemory() {
        when(embedWordVectorRepository.findAll()).thenReturn(List.of(
                EmbedWordVector.builder().word("hello").vector(List.of(1.0, 2.0)).build(),
                EmbedWordVector.builder().word("world").vector(List.of(3.0, 4.0)).build()
        ));

        service.loadCachesFromRepo();

        Map<String, float[]> cache = service.getVectorCache();
        assertEquals(2, cache.size());
        assertArrayEquals(new float[]{1f, 2f}, cache.get("hello"));
        assertArrayEquals(new float[]{3f, 4f}, cache.get("world"));
    }

    @Test
    void ensureEmbedExistsReturnsExistingEntityAndCachesNonZeroVector() {
        EmbedWordVector existing = EmbedWordVector.builder()
                .word("hello")
                .definition("a greeting")
                .vector(List.of(0.3, 0.7))
                .build();
        when(embedWordVectorRepository.findById("hello")).thenReturn(Optional.of(existing));

        EmbedWordVector result = service.ensureEmbedExists("hello");

        assertEquals(existing, result);
        assertArrayEquals(new float[]{0.3f, 0.7f}, service.getVectorCache().get("hello"));
    }

    @Test
    void ensureEmbedExistsDeletesZeroVectorAndReEmbeds() {
        EmbedWordVector zeroVector = EmbedWordVector.builder()
                .word("hello")
                .definition("old definition")
                .vector(List.of(0.0, 0.0))
                .build();
        when(embedWordVectorRepository.findById("hello")).thenReturn(Optional.of(zeroVector));
        when(findWordService.findWord("hello")).thenReturn(wordDetail("fresh definition"));
        when(embeddingService.embed("fresh definition")).thenReturn(new float[]{1f, 2f});
        when(embedWordVectorRepository.save(any(EmbedWordVector.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmbedWordVector result = service.ensureEmbedExists("hello");

        verify(embedWordVectorRepository).deleteById("hello");
        assertEquals("fresh definition", result.getDefinition());
        assertEquals(List.of(1.0, 2.0), result.getVector());
        assertArrayEquals(new float[]{1f, 2f}, service.getVectorCache().get("hello"));
    }

    @Test
    void ensureEmbedExistsReturnsNullWhenDefinitionCannotBeFetched() {
        when(embedWordVectorRepository.findById("missing")).thenReturn(Optional.empty());
        when(findWordService.findWord("missing")).thenThrow(new RuntimeException("lookup failed"));

        EmbedWordVector result = service.ensureEmbedExists("missing");

        assertNull(result);
    }

    @Test
    void ensureEmbedExistsCreatesAndCachesNewEmbedding() {
        when(embedWordVectorRepository.findById("hello")).thenReturn(Optional.empty());
        when(findWordService.findWord("hello")).thenReturn(wordDetail("a greeting"));
        when(embeddingService.embed("a greeting")).thenReturn(new float[]{0.1f, 0.2f});
        when(embedWordVectorRepository.save(any(EmbedWordVector.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmbedWordVector result = service.ensureEmbedExists("hello");

        assertNotNull(result.getCreateAt());
        assertEquals("hello", result.getWord());
        assertEquals(2, result.getVector().size());
        assertEquals(0.1d, result.getVector().get(0), 1e-6);
        assertEquals(0.2d, result.getVector().get(1), 1e-6);
        assertArrayEquals(new float[]{0.1f, 0.2f}, service.getVectorCache().get("hello"));
    }

    @Test
    void getVectorCacheReturnsUnmodifiableView() {
        when(embedWordVectorRepository.findById("hello")).thenReturn(Optional.empty());
        when(findWordService.findWord("hello")).thenReturn(wordDetail("a greeting"));
        when(embeddingService.embed("a greeting")).thenReturn(new float[]{0.1f});
        when(embedWordVectorRepository.save(any(EmbedWordVector.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service.ensureEmbedExists("hello");

        Map<String, float[]> cache = service.getVectorCache();

        assertThrows(UnsupportedOperationException.class,
                () -> cache.put("world", new float[]{2f}));
    }

    private WordDetailDto wordDetail(String definition) {
        return WordDetailDto.builder()
                .meanings(List.of(
                        WordDetailDto.MeaningDto.builder()
                                .definitions(List.of(
                                        WordDetailDto.DefinitionDto.builder()
                                                .definition(definition)
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }
}
