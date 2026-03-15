package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.SemanticResult;
import com.example.Royal_Blueberry.entity.EmbedWordVector;
import com.example.Royal_Blueberry.service.EmbedWordService;
import com.example.Royal_Blueberry.service.SemanticSearchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@RequestMapping("/api/searching/semantic")
@RestController
@Slf4j
public class SemanticSearchController {
    private final SemanticSearchService semanticSearchService ;
    private final EmbedWordService embedWordService ;

    @GetMapping
    public ResponseEntity<List<SemanticResult>> search(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "3",name = "k")   int   topK,
            @RequestParam(defaultValue = "0.3", name = "t") float threshold) {

        return ResponseEntity.ok(
                semanticSearchService.search(query, topK, threshold)
        );
    }

    @PostMapping("/embed/{word}")
    public ResponseEntity<EmbedWordVector> embedWord(@PathVariable("word") String word) {

        return ResponseEntity.ok(embedWordService.ensureEmbedExists(word));
    }
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
