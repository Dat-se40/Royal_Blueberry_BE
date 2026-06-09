package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.SemanticResult;
import com.example.Royal_Blueberry.entity.EmbedWordVector;
import com.example.Royal_Blueberry.service.EmbedWordService;
import com.example.Royal_Blueberry.service.SemanticSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticSearchControllerTest {

    @Mock
    private SemanticSearchService semanticSearchService;

    @Mock
    private EmbedWordService embedWordService;

    @InjectMocks
    private SemanticSearchController controller;

    @Test
    void searchDelegatesQueryParameters() {
        when(semanticSearchService.search("hello", 5, 0.4f))
                .thenReturn(List.of(new SemanticResult("hi", 0.9f)));

        ResponseEntity<List<SemanticResult>> response = controller.search("hello", 5, 0.4f);

        assertEquals(1, response.getBody().size());
        assertEquals("hi", response.getBody().get(0).getWord());
    }

    @Test
    void embedWordReturnsStoredEmbedding() {
        when(embedWordService.ensureEmbedExists("hello")).thenReturn(EmbedWordVector.builder()
                .word("hello")
                .definition("a greeting")
                .vector(List.of(1.0, 2.0))
                .build());

        ResponseEntity<EmbedWordVector> response = controller.embedWord("hello");

        assertEquals("hello", response.getBody().getWord());
    }

    @Test
    void initializeDataVectorAggregatesSuccessAndFailures() {
        when(embedWordService.ensureEmbedExists("hello")).thenReturn(EmbedWordVector.builder()
                .word("hello")
                .definition("a greeting")
                .vector(List.of(1.0))
                .build());
        when(embedWordService.ensureEmbedExists("broken")).thenReturn(null);
        when(embedWordService.ensureEmbedExists("error"))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<Map<String, Object>> response =
                controller.initializeDataVector(List.of("hello", "broken", "error"));

        assertEquals(3, response.getBody().get("total"));
        assertEquals(1, response.getBody().get("success"));
        assertEquals(2, response.getBody().get("failed"));
        assertEquals(List.of("broken", "error"), response.getBody().get("failedWords"));
    }
}
