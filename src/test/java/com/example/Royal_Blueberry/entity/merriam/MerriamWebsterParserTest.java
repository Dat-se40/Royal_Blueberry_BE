package com.example.Royal_Blueberry.entity.merriam;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MerriamWebsterParserTest {

    private final MerriamWebsterParser parser = new MerriamWebsterParser();

    @Test
    void parseDictionaryReturnsEmptyForSuggestionPayload() {
        assertTrue(parser.parseDictionary("[\"hello\", \"help\"]").isEmpty());
    }

    @Test
    void parseThesaurusReturnsEmptyForInvalidJson() {
        assertTrue(parser.parseThesaurus("{invalid").isEmpty());
    }

    @Test
    void buildAudioUrlUsesExpectedSubdirectories() {
        assertEquals(
                "https://media.merriam-webster.com/audio/prons/en/us/mp3/bix/bixby.mp3",
                parser.buildAudioUrl("bixby")
        );
        assertEquals(
                "https://media.merriam-webster.com/audio/prons/en/us/mp3/gg/ggplot.mp3",
                parser.buildAudioUrl("ggplot")
        );
        assertEquals(
                "https://media.merriam-webster.com/audio/prons/en/us/mp3/number/1_test.mp3",
                parser.buildAudioUrl("1_test")
        );
        assertEquals(
                "https://media.merriam-webster.com/audio/prons/en/us/mp3/h/hello.mp3",
                parser.buildAudioUrl("hello")
        );
    }

    @Test
    void cleanIdRemovesTrailingSenseSuffix() {
        assertEquals("hello", parser.cleanId("hello:2"));
        assertEquals("", parser.cleanId(null));
    }

    @Test
    void extractSynonymsAndAntonymsDeduplicatesWords() {
        String json = """
                [{
                  "meta": {"id": "happy"},
                  "def": [{
                    "sseq": [[
                      ["sense", {
                        "syn_list": [[{"wd": "glad"}, {"wd": "joyful"}], [{"wd": "glad"}]],
                        "ant_list": [[{"wd": "sad"}]]
                      }]
                    ]]
                  }]
                }]
                """;

        List<MWThesaurusEntry> entries = parser.parseThesaurus(json);
        Map<String, List<String>> result = parser.extractSynonymsAntonyms(entries);

        assertEquals(List.of("glad", "joyful"), result.get("synonyms"));
        assertEquals(List.of("sad"), result.get("antonyms"));
    }
}
