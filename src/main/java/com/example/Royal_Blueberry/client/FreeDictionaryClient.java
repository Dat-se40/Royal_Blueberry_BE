package com.example.Royal_Blueberry.client;

import com.example.Royal_Blueberry.config.FreeDictionaryConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class FreeDictionaryClient {
    private final FreeDictionaryConfig config;
    private final RestTemplate restTemplate ;

    public String fetchWord(String word)
    {
        String url = config.getUri() + word.trim();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.out.println("Free Dictionary error: " + e.getMessage());
            return null ;
        }
    }
}
