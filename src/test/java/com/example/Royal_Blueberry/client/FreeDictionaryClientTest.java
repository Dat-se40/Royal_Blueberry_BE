package com.example.Royal_Blueberry.client;

import com.example.Royal_Blueberry.config.FreeDictionaryConfig;
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
class FreeDictionaryClientTest {

    @Mock
    private FreeDictionaryConfig config;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FreeDictionaryClient client;

    @Test
    void fetchWordBuildsTrimmedUrlAndReturnsBody() {
        when(config.getUri()).thenReturn("https://free/api/");
        when(restTemplate.getForEntity("https://free/api/hello", String.class))
                .thenReturn(new ResponseEntity<>("{\"word\":\"hello\"}", HttpStatus.OK));

        String result = client.fetchWord(" hello ");

        assertEquals("{\"word\":\"hello\"}", result);
        verify(restTemplate).getForEntity("https://free/api/hello", String.class);
    }

    @Test
    void fetchWordReturnsNullWhenRestTemplateThrows() {
        when(config.getUri()).thenReturn("https://free/api/");
        when(restTemplate.getForEntity("https://free/api/hello", String.class))
                .thenThrow(new RestClientException("boom"));

        assertNull(client.fetchWord("hello"));
    }
}
