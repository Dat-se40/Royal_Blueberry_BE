package com.example.Royal_Blueberry.client;

import com.example.Royal_Blueberry.config.FreeDictionaryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
@Slf4j
public class FreeDictionaryClient {
    private final FreeDictionaryConfig config;
    private final RestTemplate restTemplate ;

    public String fetchWord(String word)
    {
        String url = config.getUri() + word.trim();
        log.debug("[FreeDictionary] Fetching word: '{}'", word);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.debug("[FreeDictionary] Response status={} for word='{}'",
                    response.getStatusCode(), word);
            return response.getBody();
        } catch (RestClientException e) {
            log.warn("[FreeDictionary] Error fetching word '{}': {}", word, e.getMessage());
            return null ;
        }
    }
}
