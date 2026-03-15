package com.example.Royal_Blueberry.service;

import com.example.Royal_Blueberry.dto.WordDetailDto;
import com.example.Royal_Blueberry.dto.WordEntryDto;
import com.example.Royal_Blueberry.entity.EmbedWordVector;

import java.util.List;
import java.util.Map;

public interface EmbedWordService {
    void loadCachesFromRepo();                        // @PostConstruct
    EmbedWordVector ensureEmbedExists(String word);   // đổi tên cho rõ
    Map<String, float[]> getVectorCache();
}
