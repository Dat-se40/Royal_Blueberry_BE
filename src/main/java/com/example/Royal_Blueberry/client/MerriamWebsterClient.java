package com.example.Royal_Blueberry.client;

import com.example.Royal_Blueberry.config.MerriamWebsterConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class MerriamWebsterClient
{
    private final MerriamWebsterConfig config ;
    private final RestTemplate restTemplate;

    public String fetchDictionary(String word)
    {
        String url = config.getDictUri() + "/" + word + "?key=" + config.getDictKey();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.out.println("MW Dictionary error: " + e.getMessage());
            return null ;
        }
    }
    public String fetchThesaurus(String word)
    {
        String url = config.getThesaurusUri() + "/" + word + "?key=" + config.getThesaurusKey();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.out.println("MW Dictionary error: " + e.getMessage());
            return null ;
        }
    }
}
