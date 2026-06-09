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
                "hwi": {"hw": "be", "prs": [{"mw": "'bē"}]},
                "ins": [
                  {"il": "present tense third-person singular", "if": "is", "prs": [{"mw": "'iz", "sound": {"audio": "be000008"}}]},
                  {"il": "past tense first-person singular", "if": "was", "prs": [{"mw": "'wəz", "sound": {"audio": "be000002"}}]}
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
              "hwi": {"hw": "and", "prs": [{"mw": "ən(d)", "sound": {"audio": "and00001"}}]},
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

        assertEquals("ən(d)", result.getPhonetic());
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
}
