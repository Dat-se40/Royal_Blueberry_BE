package com.example.Royal_Blueberry.testClient;

import com.example.Royal_Blueberry.client.MerriamWebsterClient;
import com.example.Royal_Blueberry.config.MerriamWebsterConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MerriamWebsterClientTest {

    @Mock
    private MerriamWebsterConfig config;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MerriamWebsterClient merriamWebsterClient;

    @Test
    void fetchDictionaryReturnsBodyFromConfiguredUrl() {
        when(config.getDictUri()).thenReturn("https://mw/dictionary");
        when(config.getDictKey()).thenReturn("dict-key");
        when(restTemplate.getForEntity("https://mw/dictionary/hello?key=dict-key", String.class))
                .thenReturn(new ResponseEntity<>("[{\"meta\":{}}]", HttpStatus.OK));

        String result = merriamWebsterClient.fetchDictionary("hello");

        assertEquals("[{\"meta\":{}}]", result);
        verify(restTemplate).getForEntity("https://mw/dictionary/hello?key=dict-key", String.class);
    }

    @Test
    void fetchDictionaryReturnsNullOnRestClientException() {
        when(config.getDictUri()).thenReturn("https://mw/dictionary");
        when(config.getDictKey()).thenReturn("dict-key");
        when(restTemplate.getForEntity("https://mw/dictionary/hello?key=dict-key", String.class))
                .thenThrow(new RestClientException("boom"));

        assertNull(merriamWebsterClient.fetchDictionary("hello"));
    }

    @Test
    void fetchThesaurusReturnsBodyFromConfiguredUrl() {
        when(config.getThesaurusUri()).thenReturn("https://mw/thesaurus");
        when(config.getThesaurusKey()).thenReturn("th-key");
        when(restTemplate.getForEntity("https://mw/thesaurus/hello?key=th-key", String.class))
                .thenReturn(new ResponseEntity<>("[{\"meta\":{}}]", HttpStatus.OK));

        String result = merriamWebsterClient.fetchThesaurus("hello");

        assertEquals("[{\"meta\":{}}]", result);
        verify(restTemplate).getForEntity("https://mw/thesaurus/hello?key=th-key", String.class);
    }
}
