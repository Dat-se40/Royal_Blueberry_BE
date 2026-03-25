package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.dto.WordDetailDto;
import com.example.Royal_Blueberry.dto.WordEntryDto;
import com.example.Royal_Blueberry.entity.EmbedWordVector;
import com.example.Royal_Blueberry.entity.WordEntry;
import com.example.Royal_Blueberry.repository.EmbedWordVectorRepository;
import com.example.Royal_Blueberry.service.EmbedWordService;
import com.example.Royal_Blueberry.service.FindWordService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!no_ai")
public class EmbedWordServiceImpl implements EmbedWordService {

    private final EmbedWordVectorRepository embedWordVectorRepository;
    private final EmbeddingService embeddingService;       // gọi model
    private final FindWordService findWordService;         // fetch definition

    private final Map<String, float[]> vectorCache = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    @Override
    public void loadCachesFromRepo() {
        long startTime = System.currentTimeMillis() ;
        System.out.println("[EmbedWordService] Loading vector cache...");
        embedWordVectorRepository.findAll().forEach(ev ->
                vectorCache.put(ev.getWord(), toFloatArray(ev.getVector()))
        );
        System.out.println("[EmbedWordService] had Loaded "  + vectorCache.size() +
                " vectors for " + (System.currentTimeMillis() - startTime) + "ms" );
    }

    @Override
    public EmbedWordVector ensureEmbedExists(String word) {

        // 1. Đã có trong DB → load cache nếu thiếu → return
        var existing = embedWordVectorRepository.findById(word);
        if (existing.isPresent()) {
            if (!isZeroVector(existing.get().getVector())) {
                vectorCache.putIfAbsent(word, toFloatArray(existing.get().getVector()));
                return existing.get();
            }
            // Vector = 0 → xóa và tính lại
            embedWordVectorRepository.deleteById(word);
            log.warn("[EmbedWordService] Re-embedding '{}' (was zero vector)", word);
        }

        // 2. Fetch definition từ FindWordService
        String definition = fetchDefinition(word);
        if (definition == null) {
            return null;
        }

        // 3. Gọi model tạo vector
        float[] vector = embeddingService.embed(definition);

        // 4. Lưu MongoDB
        EmbedWordVector entity = EmbedWordVector.builder()
                .word(word)
                .definition(definition)
                .vector(toDoubleList(vector))
                .createAt(LocalDateTime.now())
                .build();
        embedWordVectorRepository.save(entity);

        // 5. Lưu cache
        vectorCache.put(word, vector);

        return entity;
    }

    @Override
    public Map<String, float[]> getVectorCache() {
        return Collections.unmodifiableMap(vectorCache);
    }

    // ── Helpers ──────────────────────────────────────────────────

    private String fetchDefinition(String word) {
        try {
            WordDetailDto dto = findWordService.findWord(word);
            return dto.getMeanings().get(0)
                    .getDefinitions().get(0)
                    .getDefinition();
        } catch (Exception e) {
            return null;
        }
    }

    private float[] toFloatArray(List<Double> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++)
            arr[i] = list.get(i).floatValue();
        return arr;
    }

    private List<Double> toDoubleList(float[] arr) {
        List<Double> list = new ArrayList<>();
        for (float f : arr) list.add((double) f);
        return list;
    }
    private boolean isZeroVector(List<Double> vector) {
        return vector == null || vector.stream().allMatch(v -> v == 0.0);
    }
}
