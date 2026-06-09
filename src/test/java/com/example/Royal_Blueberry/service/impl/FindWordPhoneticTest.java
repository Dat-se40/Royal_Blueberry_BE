package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.client.FreeDictionaryClient;
import com.example.Royal_Blueberry.client.MerriamWebsterClient;
import com.example.Royal_Blueberry.dto.WordDetailDto;
import com.example.Royal_Blueberry.entity.merriam.MerriamWebsterParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindWordPhoneticTest {

    @Mock
    private MerriamWebsterClient mwClient;

    @Mock
    private FreeDictionaryClient freeClient;

    private FindWordServiceImpl findWordService;

    private static final String IS_MW_JSON = """
            [
              {
                "meta": {"id": "is:1", "stems": ["be", "is"]},
                "hwi": {"hw": "is"},
                "cxs": [{"cxl": "present tense third-person singular of", "cxtis": [{"cxt": "be"}]}],
                "shortdef": []
              },
              {
                "fl": "verb",
                "hwi": {"hw": "be", "prs": [{"mw": "'bee"}]},
                "ins": [
                  {"il": "present tense third-person singular", "if": "is", "prs": [{"mw": "'iz", "sound": {"audio": "be000008"}}]},
                  {"il": "past tense first-person singular", "if": "was", "prs": [{"mw": "'waz", "sound": {"audio": "be000002"}}]}
                ],
                "shortdef": ["to have identity"]
              }
            ]
            """;

    private static final String AND_FREE_JSON = """
            [{
              "word": "and",
              "phonetic": "",
              "phonetics": [{"text": "", "audio": "https://example.com/and.mp3"}],
              "meanings": [{"partOfSpeech": "conjunction", "definitions": [{"definition": "used to connect"}]}]
            }]
            """;

    private static final String AND_MW_JSON = """
            [{
              "fl": "conjunction",
              "hwi": {"hw": "and", "prs": [{"mw": "uhn(d)", "sound": {"audio": "and00001"}}]},
              "shortdef": ["used as a function word"]
            }]
            """;

    private static final String AND_FREE_NO_AUDIO_JSON = """
            [{
              "word": "and",
              "phonetic": "",
              "phonetics": [{"text": ""}],
              "meanings": [{"partOfSpeech": "conjunction", "definitions": [{"definition": "used to connect"}]}]
            }]
            """;

    @BeforeEach
    void setUp() {
        findWordService = new FindWordServiceImpl(
                mwClient,
                freeClient,
                new MerriamWebsterParser(),
                new ObjectMapper()
        );
    }

    @Test
    void inflectedFormUsesInsPronunciationWhenHeadwordHasNoPrs() {
        when(mwClient.fetchDictionary("is")).thenReturn(IS_MW_JSON);
        when(mwClient.fetchThesaurus(anyString())).thenReturn("[]");
        when(freeClient.fetchWord("is")).thenReturn(null);

        WordDetailDto result = findWordService.findWord("is");

        assertEquals("'iz", result.getPhonetic());
        assertEquals(
                "https://media.merriam-webster.com/audio/prons/en/us/mp3/b/be000008.mp3",
                result.getAudioUs()
        );
    }

    @Test
    void emptyFreePhoneticFallsBackToMwHeadword() {
        when(mwClient.fetchDictionary("and")).thenReturn(AND_MW_JSON);
        when(mwClient.fetchThesaurus(anyString())).thenReturn("[]");
        when(freeClient.fetchWord("and")).thenReturn(AND_FREE_JSON);

        WordDetailDto result = findWordService.findWord("and");

        assertEquals("uhn(d)", result.getPhonetic());
        assertEquals("https://example.com/and.mp3", result.getAudioUs());
    }

    @Test
    void emptyFreeAudioFallsBackToMwHeadwordAudio() {
        when(mwClient.fetchDictionary("and")).thenReturn(AND_MW_JSON);
        when(mwClient.fetchThesaurus(anyString())).thenReturn("[]");
        when(freeClient.fetchWord("and")).thenReturn(AND_FREE_NO_AUDIO_JSON);

        WordDetailDto result = findWordService.findWord("and");

        assertEquals(
                "https://media.merriam-webster.com/audio/prons/en/us/mp3/a/and00001.mp3",
                result.getAudioUs()
        );
        assertNotNull(result.getAudioUk());
    }

    @Test
    void fallsBackToFreeOnlyMeaningsWhenMwHasNoEntries() {
        when(mwClient.fetchDictionary("hello")).thenReturn("[]");
        when(mwClient.fetchThesaurus("hello")).thenReturn("[]");
        when(freeClient.fetchWord("hello")).thenReturn("""
                [{
                  "word": "hello",
                  "phonetic": "/huh-loh/",
                  "phonetics": [{"text": "/huh-loh/", "audio": "https://audio.example/hello.mp3"}],
                  "meanings": [{
                    "partOfSpeech": "interjection",
                    "synonyms": ["hi"],
                    "antonyms": ["goodbye"],
                    "definitions": [{
                      "definition": "used as a greeting",
                      "example": "Hello there",
                      "synonyms": ["greeting"],
                      "antonyms": []
                    }]
                  }]
                }]
                """);

        WordDetailDto result = findWordService.findWord("hello");

        assertEquals("/huh-loh/", result.getPhonetic());
        assertEquals(1, result.getMeanings().size());
        assertEquals("interjection", result.getMeanings().get(0).getPartOfSpeech());
        assertEquals("used as a greeting",
                result.getMeanings().get(0).getDefinitions().get(0).getDefinition());
        assertEquals("Hello there",
                result.getMeanings().get(0).getDefinitions().get(0).getExample());
        assertEquals("hi", result.getMeanings().get(0).getSynonyms().get(0));
    }

    @Test
    void mergesMwDefinitionsWithFreeExamplesAndDefinitionLevelRelations() {
        when(mwClient.fetchDictionary("run")).thenReturn("""
                [{
                  "fl": "verb",
                  "hwi": {"hw": "run", "prs": [{"mw": "run-phonetic"}]},
                  "shortdef": ["to move swiftly", "to operate"]
                }]
                """);
        when(mwClient.fetchThesaurus("run")).thenReturn("""
                [{
                  "def": [{
                    "sseq": [[["sense", {
                      "syn_list": [[{"wd": "race"}]],
                      "ant_list": [[{"wd": "walk"}]]
                    }]]]
                  }]
                }]
                """);
        when(freeClient.fetchWord("run")).thenReturn("""
                [{
                  "word": "run",
                  "phonetics": [{"audio": "https://audio.example/run.mp3"}],
                  "meanings": [{
                    "partOfSpeech": "verb",
                    "definitions": [
                      {
                        "definition": "move fast",
                        "example": "I run every morning",
                        "synonyms": ["sprint"],
                        "antonyms": ["walk"]
                      },
                      {
                        "definition": "manage",
                        "example": "She runs the shop",
                        "synonyms": ["operate"],
                        "antonyms": []
                      }
                    ]
                  }]
                }]
                """);

        WordDetailDto result = findWordService.findWord("run");

        assertEquals("run-phonetic", result.getPhonetic());
        assertEquals("I run every morning",
                result.getMeanings().get(0).getDefinitions().get(0).getExample());
        assertEquals("sprint",
                result.getMeanings().get(0).getDefinitions().get(0).getSynonyms().get(0));
        assertEquals("race", result.getMeanings().get(0).getSynonyms().get(0));
        assertEquals("walk", result.getMeanings().get(0).getAntonyms().get(0));
    }

    @Test
    void invalidFreeJsonDoesNotBreakMwResultAndAudioCanStayNull() {
        when(mwClient.fetchDictionary("book")).thenReturn("""
                [{
                  "fl": "noun",
                  "hwi": {"hw": "book", "prs": [{"mw": "book-phonetic"}]},
                  "shortdef": ["a set of written sheets"]
                }]
                """);
        when(mwClient.fetchThesaurus("book")).thenReturn("[]");
        when(freeClient.fetchWord("book")).thenReturn("{invalid");

        WordDetailDto result = findWordService.findWord("book");

        assertEquals("book-phonetic", result.getPhonetic());
        assertEquals(1, result.getMeanings().size());
        assertNull(result.getAudioUs());
        assertNull(result.getAudioUk());
    }
}
