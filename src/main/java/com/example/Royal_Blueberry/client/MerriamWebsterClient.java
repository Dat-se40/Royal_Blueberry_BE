package com.example.Royal_Blueberry.client;

import com.example.Royal_Blueberry.config.MerriamWebsterConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
@Slf4j
public class MerriamWebsterClient
{
    private final MerriamWebsterConfig config ;
    private final RestTemplate restTemplate;

    public String fetchDictionary(String word)
    {
        String url = config.getDictUri() + "/" + word + "?key=" + config.getDictKey();
        log.debug("[MerriamWebster] Fetching dictionary for word: '{}'", word);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.debug("[MerriamWebster] Dictionary response status={} for word='{}'",
                    response.getStatusCode(), word);
            return response.getBody();
        } catch (RestClientException e) {
            log.warn("[MerriamWebster] Dictionary error for word '{}': {}", word, e.getMessage());
            return null ;
        }
    }
    public String fetchThesaurus(String word)
    {
        String url = config.getThesaurusUri() + "/" + word + "?key=" + config.getThesaurusKey();
        log.debug("[MerriamWebster] Fetching thesaurus for word: '{}'", word);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.debug("[MerriamWebster] Thesaurus response status={} for word='{}'",
                    response.getStatusCode(), word);
            return response.getBody();
        } catch (RestClientException e) {
            log.warn("[MerriamWebster] Thesaurus error for word '{}': {}", word, e.getMessage());
            return null ;
        }
    }
}
