package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.dto.SemanticResult;
import com.example.Royal_Blueberry.service.EmbedWordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticSearchServiceImplTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private EmbedWordService embedWordService;

    @InjectMocks
    private SemanticSearchServiceImpl service;

    @Test
    void searchReturnsEmptyWhenCacheIsEmpty() {
        when(embeddingService.embed("hello")).thenReturn(new float[]{1f, 0f});
        when(embedWordService.getVectorCache()).thenReturn(Map.of());

        List<SemanticResult> results = service.search("hello", 3, 0.1f);

        assertTrue(results.isEmpty());
    }

    @Test
    void searchFiltersSortsAndLimitsResults() {
        when(embeddingService.embed("hello")).thenReturn(new float[]{1f, 0f});

        Map<String, float[]> cache = new LinkedHashMap<>();
        cache.put("hi", new float[]{1f, 0f});
        cache.put("wave", new float[]{0.5f, 0.5f});
        cache.put("bye", new float[]{-1f, 0f});
        when(embedWordService.getVectorCache()).thenReturn(cache);

        List<SemanticResult> results = service.search("hello", 2, 0.3f);

        assertEquals(2, results.size());
        assertEquals("hi", results.get(0).getWord());
        assertTrue(results.get(0).getScore() > results.get(1).getScore());
        assertEquals("wave", results.get(1).getWord());
    }

    @Test
    void zeroMagnitudeVectorsProduceZeroSimilarity() {
        when(embeddingService.embed("hello")).thenReturn(new float[]{0f, 0f});
        when(embedWordService.getVectorCache()).thenReturn(Map.of("hi", new float[]{1f, 0f}));

        List<SemanticResult> results = service.search("hello", 3, 0.0f);

        assertEquals(1, results.size());
        assertEquals(0.0f, results.get(0).getScore());
    }
}
