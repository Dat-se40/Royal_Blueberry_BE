package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.SemanticResult;
import com.example.Royal_Blueberry.entity.EmbedWordVector;
import com.example.Royal_Blueberry.service.EmbedWordService;
import com.example.Royal_Blueberry.service.SemanticSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@RequestMapping("/api/searching/semantic")
@RestController
@Slf4j
@Profile("!no_ai")
@Tag(name = "Semantic Search", description = "Semantic search and embedding management endpoints")
public class SemanticSearchController {
    private final SemanticSearchService semanticSearchService ;
    private final EmbedWordService embedWordService ;

    @Operation(
            summary = "Basic semantic search",
            description = """
                    Find semantically similar words to a query using neural embeddings.
                    
                    **Parameters:**
                    - `q`: Search query (the word or phrase to find similar words for)
                    - `k`: Number of results to return (default: 3)
                    - `t`: Similarity threshold (0.0-1.0, default: 0.3)
                    
                    **Score interpretation:**
                    - 1.0 = Identical meaning
                    - 0.8+ = Very similar
                    - 0.5-0.8 = Related
                    - 0.3-0.5 = Somewhat related
                    - <0.3 = Not related (filtered out by default)
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of semantically similar words",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "word": "hi",
                                        "score": 0.92
                                      },
                                      {
                                        "word": "hey",
                                        "score": 0.88
                                      },
                                      {
                                        "word": "greet",
                                        "score": 0.75
                                      }
                                    ]
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameters",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)

            )
    })
    @GetMapping
    public ResponseEntity<List<SemanticResult>> search(
            @io.swagger.v3.oas.annotations.Parameter(
                    name = "q",
                    description = "Search query (word or phrase)",
                    example = "hello",
                    required = true
            )
            @RequestParam("q") String query,

            @io.swagger.v3.oas.annotations.Parameter(
                    name = "k",
                    description = "Number of results to return",
                    example = "5",
                    required = false
            )
            @RequestParam(defaultValue = "3", name = "k") int topK,

            @io.swagger.v3.oas.annotations.Parameter(
                    name = "t",
                    description = "Similarity threshold (0.0-1.0)",
                    example = "0.3",
                    required = false
            )
            @RequestParam(defaultValue = "0.3", name = "t") float threshold) {

        log.info("[SemanticSearch] Basic search: q='{}', k={}, t={}", query, topK, threshold);

        List<SemanticResult> results = semanticSearchService.search(query, topK, threshold);
        return ResponseEntity.ok(results);
    }

    @Operation(
            summary = "Generate embedding for one word",
            description = "Creates the semantic vector for a word if it does not already exist, then returns the stored embedding.",
            security = @SecurityRequirement(name = com.example.Royal_Blueberry.config.OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Embedding returned",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EmbedWordVector.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping("/embed/{word}")
    public ResponseEntity<EmbedWordVector> embedWord(@PathVariable("word") String word) {

        return ResponseEntity.ok(embedWordService.ensureEmbedExists(word));
    }

    @Operation(
            summary = "Initialize embeddings in bulk",
            description = """
                    Creates embeddings for a list of words and returns a processing summary,
                    including total items, success count, failed count, and execution time.
                    """,
            security = @SecurityRequirement(name = com.example.Royal_Blueberry.config.OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Bulk initialization completed",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "total": 100,
                                      "success": 96,
                                      "failed": 4,
                                      "failedWords": ["typo1", "typo2"],
                                      "totalTimeMs": 8200,
                                      "totalTimeSec": 8.2,
                                      "avgMsPerWord": 85
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping("/embed/initialize")
    public ResponseEntity<Map<String, Object>> initializeDataVector(@RequestBody List<String> list) {

        long startTime = System.currentTimeMillis();
        log.info("[EmbedInit] Starting embed for {} words...", list.size());

        List<String> success = new ArrayList<>();
        List<String> failed  = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            String word = list.get(i);
            try {
                var result = embedWordService.ensureEmbedExists(word);
                if (result != null) {
                    success.add(result.getWord());
                } else {
                    failed.add(word);
                    log.warn("[EmbedInit] [{}/{}] FAILED (null): {}", i + 1, list.size(), word);
                }
            } catch (Exception e) {
                failed.add(word);
                log.warn("[EmbedInit] [{}/{}] ERROR: {} — {}", i + 1, list.size(), word, e.getMessage());
            }

            // Log tiến độ mỗi 50 từ
            if ((i + 1) % 50 == 0) {
                long elapsed = System.currentTimeMillis() - startTime;
                log.info("[EmbedInit] Progress: {}/{} | Elapsed: {}s | Avg: {}ms/word",
                        i + 1, list.size(),
                        elapsed / 1000,
                        elapsed / (i + 1)
                );
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("total",        list.size());
        response.put("success",      success.size());
        response.put("failed",       failed.size());
        response.put("failedWords",  failed);
        response.put("totalTimeMs",  totalTime);
        response.put("totalTimeSec", totalTime / 1000.0);
        response.put("avgMsPerWord", success.isEmpty() ? 0 : totalTime / success.size());

        log.info("[EmbedInit] Done! {}/{} success | Total: {}s | Avg: {}ms/word",
                success.size(), list.size(),
                totalTime / 1000.0,
                success.isEmpty() ? 0 : totalTime / success.size()
        );

        return ResponseEntity.ok(response);
    }
}
