package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.client.FreeDictionaryClient;
import com.example.Royal_Blueberry.client.MerriamWebsterClient;
import com.example.Royal_Blueberry.dto.WordDetailDto;
import com.example.Royal_Blueberry.entity.free.FreeDefinition;
import com.example.Royal_Blueberry.entity.free.FreeEntry;
import com.example.Royal_Blueberry.entity.free.FreeMeaning;
import com.example.Royal_Blueberry.entity.free.FreePhonetic;
import com.example.Royal_Blueberry.entity.merriam.MWEntry;
import com.example.Royal_Blueberry.entity.merriam.MWThesaurusEntry;
import com.example.Royal_Blueberry.entity.merriam.MerriamWebsterParser;
import com.example.Royal_Blueberry.service.FindWordService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class FindWordServiceImpl implements FindWordService {

    private final MerriamWebsterClient mwClient;
    private final FreeDictionaryClient freeClient;
    private final MerriamWebsterParser mwParser;
    private final ObjectMapper objectMapper;

    @Override
    public WordDetailDto findWord(String word) {
        log.info("[FindWordService] Looking up word: '{}'", word);
        long startTime = System.currentTimeMillis();

        // Gọi song song 3 API
        CompletableFuture<String> mwDictFuture =
                CompletableFuture.supplyAsync(() -> mwClient.fetchDictionary(word));

        CompletableFuture<String> mwThesaurusFuture =
                CompletableFuture.supplyAsync(() -> mwClient.fetchThesaurus(word));

        CompletableFuture<String> freeFuture =
                CompletableFuture.supplyAsync(() -> freeClient.fetchWord(word));

        CompletableFuture.allOf(mwDictFuture, mwThesaurusFuture, freeFuture).join();

        List<MWEntry> mwEntries = mwParser.parseDictionary(mwDictFuture.join());
        List<MWThesaurusEntry> mwThesaurus = mwParser.parseThesaurus(mwThesaurusFuture.join());
        List<FreeEntry> freeEntries = parseFree(freeFuture.join());

        log.debug("[FindWordService] API results - MW entries={}, MW thesaurus={}, Free entries={}",
                mwEntries.size(), mwThesaurus.size(), freeEntries.size());

        WordDetailDto result = merge(word, mwEntries, mwThesaurus, freeEntries);
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("[FindWordService] Word '{}' resolved in {}ms - meanings={}",
                word, elapsed, result.getMeanings() != null ? result.getMeanings().size() : 0);

        return result;
    }

    private WordDetailDto merge(
            String word,
            List<MWEntry> mwEntries,
            List<MWThesaurusEntry> mwThesaurus,
            List<FreeEntry> freeEntries) {

        FreeEntry freeEntry = freeEntries.isEmpty() ? null : freeEntries.get(0);
        var synAnt = mwParser.extractSynonymsAntonyms(mwThesaurus);

        // phonetic: Free đẹp hơn MW
        String phonetic = freeEntry != null ? freeEntry.getPhonetic() : extractMwPhonetic(mwEntries);

        // audio: lấy từ Free phonetics
        String audioUs = extractAudio(freeEntry, "en-US");
        String audioUk = extractAudio(freeEntry, "en-GB");

        // Merge meanings - ưu tiên structure của Free, bổ sung example từ Free
        List<WordDetailDto.MeaningDto> meanings = buildMeanings(mwEntries, freeEntry, synAnt);

        return WordDetailDto.builder()
                .word(word)
                .phonetic(phonetic)
                .audioUs(audioUs)
                .audioUk(audioUk)
                .imageUrl(null)     // sẽ điền sau khi có ImageSearchService
                .meanings(meanings)
                .build();
    }

    private List<WordDetailDto.MeaningDto> buildMeanings(
            List<MWEntry> mwEntries,
            FreeEntry freeEntry,
            Map<String, List<String>> synAnt) {

        List<WordDetailDto.MeaningDto> result = new ArrayList<>();

        // Map partOfSpeech → definitions từ Free (để lookup example)
        Map<String, List<FreeDefinition>> freeDefMap = buildFreeDefMap(freeEntry);

        int meaningIndex = 0;

        // Duyệt MW entries (mỗi entry = 1 meaning/partOfSpeech)
        for (MWEntry entry : mwEntries) {
            String pos = entry.getFunctionalLabel();
            if (pos == null) continue;

            List<WordDetailDto.DefinitionDto> definitions = new ArrayList<>();
            List<FreeDefinition> freeDefs = freeDefMap.getOrDefault(pos, List.of());

            // Build definitions từ MW shortdef, bổ sung example từ Free
            if (entry.getShortDefinitions() != null) {
                for (int i = 0; i < entry.getShortDefinitions().size(); i++) {
                    String example = (i < freeDefs.size()) ? freeDefs.get(i).getExample() : null;
                    List<String> defSynonyms = (i < freeDefs.size()) ? freeDefs.get(i).getSynonyms() : List.of();
                    List<String> defAntonyms = (i < freeDefs.size()) ? freeDefs.get(i).getAntonyms() : List.of();

                    definitions.add(WordDetailDto.DefinitionDto.builder()
                            .definitionIndex(i)
                            .definition(entry.getShortDefinitions().get(i))
                            .example(example)
                            .synonyms(defSynonyms != null ? defSynonyms : List.of())
                            .antonyms(defAntonyms != null ? defAntonyms : List.of())
                            .build());
                }
            }

            result.add(WordDetailDto.MeaningDto.builder()
                    .meaningIndex(meaningIndex++)
                    .partOfSpeech(pos)
                    .definitions(definitions)
                    .synonyms(synAnt.getOrDefault("synonyms", List.of()))
                    .antonyms(synAnt.getOrDefault("antonyms", List.of()))
                    .build());
        }

        // Nếu MW không có gì, fallback về Free hoàn toàn
        if (result.isEmpty() && freeEntry != null) {
            result = buildMeaningsFromFreeOnly(freeEntry);
        }

        return result;
    }

    // ─── Helpers ──────────────────────────────────────────────────

    private Map<String, List<FreeDefinition>> buildFreeDefMap(FreeEntry freeEntry) {
        if (freeEntry == null || freeEntry.getMeanings() == null) return Map.of();

        Map<String, List<FreeDefinition>> map = new LinkedHashMap<>();
        for (FreeMeaning m : freeEntry.getMeanings()) {
            if (m.getPartOfSpeech() != null && m.getDefinitions() != null) {
                map.put(m.getPartOfSpeech(), m.getDefinitions());
            }
        }
        return map;
    }

    private List<WordDetailDto.MeaningDto> buildMeaningsFromFreeOnly(FreeEntry freeEntry) {
        List<WordDetailDto.MeaningDto> result = new ArrayList<>();
        int idx = 0;

        for (FreeMeaning m : freeEntry.getMeanings()) {
            List<WordDetailDto.DefinitionDto> defs = new ArrayList<>();
            if (m.getDefinitions() != null) {
                for (int i = 0; i < m.getDefinitions().size(); i++) {
                    FreeDefinition fd = m.getDefinitions().get(i);
                    defs.add(WordDetailDto.DefinitionDto.builder()
                            .definitionIndex(i)
                            .definition(fd.getDefinition())
                            .example(fd.getExample())
                            .synonyms(fd.getSynonyms() != null ? fd.getSynonyms() : List.of())
                            .antonyms(fd.getAntonyms() != null ? fd.getAntonyms() : List.of())
                            .build());
                }
            }

            result.add(WordDetailDto.MeaningDto.builder()
                    .meaningIndex(idx++)
                    .partOfSpeech(m.getPartOfSpeech())
                    .definitions(defs)
                    .synonyms(m.getSynonyms() != null ? m.getSynonyms() : List.of())
                    .antonyms(m.getAntonyms() != null ? m.getAntonyms() : List.of())
                    .build());
        }

        return result;
    }

    private String extractMwPhonetic(List<MWEntry> entries) {
        if (entries.isEmpty()) return null;
        MWEntry first = entries.get(0);
        if (first.getHwi() == null || first.getHwi().getPronunciations() == null
                || first.getHwi().getPronunciations().isEmpty()) return null;
        return first.getHwi().getPronunciations().get(0).getMw();
    }

    private String extractAudio(FreeEntry freeEntry, String locale) {
        if (freeEntry == null || freeEntry.getPhonetics() == null) return null;

        return freeEntry.getPhonetics().stream()
                .filter(p -> p.getAudio() != null && p.getAudio().contains(locale.toLowerCase().replace("-", "-")))
                .map(FreePhonetic::getAudio)
                .findFirst()
                // fallback: lấy audio bất kỳ có URL
                .orElseGet(() -> freeEntry.getPhonetics().stream()
                        .map(FreePhonetic::getAudio)
                        .filter(a -> a != null && !a.isBlank())
                        .findFirst()
                        .orElse(null));
    }

    private List<FreeEntry> parseFree(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            return objectMapper.readValue(json, new TypeReference<List<FreeEntry>>() {});
        } catch (Exception e) {
            log.warn("[FindWordService] Free Dictionary parse error: {}", e.getMessage());
            return List.of();
        }
    }
}