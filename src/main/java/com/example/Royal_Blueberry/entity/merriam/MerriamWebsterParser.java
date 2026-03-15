package com.example.Royal_Blueberry.entity.merriam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MerriamWebsterParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<MWEntry> parseDictionary(String json) {
        try {
            // Guard: MW trả List<String> khi key sai hoặc word không tồn tại
            if (json == null || json.trim().startsWith("[\"")) return List.of();

            return mapper.readValue(json, new TypeReference<List<MWEntry>>() {});
        } catch (Exception e) {
            System.out.println("MW parse error: " + e.getMessage());
            return List.of();
        }
    }

    public List<MWThesaurusEntry> parseThesaurus(String json) {
        try {
            if (json == null || json.trim().startsWith("[\"")) return List.of();

            return mapper.readValue(json, new TypeReference<List<MWThesaurusEntry>>() {});
        } catch (Exception e) {
            System.out.println("MW thesaurus parse error: " + e.getMessage());
            return List.of();
        }
    }

    public String buildAudioUrl(String filename) {
        if (filename == null || filename.isBlank()) return "";

        String subdir;
        if (filename.startsWith("bix"))        subdir = "bix";
        else if (filename.startsWith("gg"))    subdir = "gg";
        else if (filename.matches("^[0-9_].*")) subdir = "number";
        else                                   subdir = String.valueOf(filename.charAt(0));

        return "https://media.merriam-webster.com/audio/prons/en/us/mp3/"
                + subdir + "/" + filename + ".mp3";
    }

    public String cleanId(String id) {
        if (id == null) return "";
        return id.replaceAll(":\\d+$", "");
    }

    // Extract synonyms/antonyms từ thesaurus sseq
    public Map<String, List<String>> extractSynonymsAntonyms(List<MWThesaurusEntry> entries) {
        Set<String> synonyms = new LinkedHashSet<>();
        Set<String> antonyms = new LinkedHashSet<>();

        for (var entry : entries) {
            if (entry.getDefinitions() == null) continue;
            for (var def : entry.getDefinitions()) {
                if (def.getSseq() == null) continue;
                for (var sseqGroup : def.getSseq()) {
                    for (var item : sseqGroup) {
                        if (item.size() < 2) continue;
                        if (!"sense".equals(item.get(0).asText())) continue;

                        JsonNode sense = item.get(1);
                        extractWordList(sense.get("syn_list"), synonyms);
                        extractWordList(sense.get("ant_list"), antonyms);
                    }
                }
            }
        }

        return Map.of("synonyms", new ArrayList<>(synonyms),
                "antonyms", new ArrayList<>(antonyms));
    }

    private void extractWordList(JsonNode list, Set<String> output) {
        if (list == null || !list.isArray()) return;
        for (JsonNode group : list) {
            for (JsonNode item : group) {
                JsonNode wd = item.get("wd");
                if (wd != null) output.add(wd.asText());
            }
        }
    }
}