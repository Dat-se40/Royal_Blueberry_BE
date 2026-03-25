package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.dto.SemanticResult;
import com.example.Royal_Blueberry.service.EmbedWordService;
import com.example.Royal_Blueberry.service.SemanticSearchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Math.sqrt;

@Service
@AllArgsConstructor
@Slf4j
@Profile("!no_ai")
public class SemanticSearchServiceImpl  implements SemanticSearchService {
    private final EmbeddingService embeddingService ;
    private final EmbedWordService embedWordService ;

    /**
     * find similar words' semantic  with query text
     * @param query  text
     * @param topK   number of result
     * @param threshold  acceptable error threshold
     */
    @Override
    public List<SemanticResult> search(String query, int topK, float threshold) {
        float[] vector = embeddingService.embed(query);

        Map<String, float[]> vectorCache = embedWordService.getVectorCache() ;
        if(vectorCache.isEmpty())
        {
            log.warn("[Semantic Service] Vector cache is empty");
            return List.of();
        }
        return vectorCache.entrySet().stream().map( entry ->
        {
            float score = cosineSimilarity(vector,entry.getValue());
            return new SemanticResult(entry.getKey(), score);
        }).filter(r -> r.getScore() >= threshold).
                sorted(Comparator.comparingDouble(SemanticResult::getScore).
                        reversed()).limit(topK).collect(Collectors.toList());
    }
    private float cosineSimilarity(float[] vectorA , float[] vectorB)
    {
        float distanceA = 0 , distanceB = 0 , accumulation  =0 ;
        for(int i  = 0 ; i < vectorA.length ; i ++)
        {
            accumulation += vectorA[i]*vectorB[i];
            distanceA += vectorA[i]*vectorA[i] ;
            distanceB += vectorB[i]*vectorB[i] ;
        }
        if (distanceA == 0 || distanceB == 0 ) return 0 ;
        return (float) (accumulation/(sqrt(distanceA)*sqrt(distanceB)));
    }
}
