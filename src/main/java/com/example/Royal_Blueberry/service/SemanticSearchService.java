package com.example.Royal_Blueberry.service;

import com.example.Royal_Blueberry.dto.SemanticResult;

import java.util.List;

public interface SemanticSearchService {
    List<SemanticResult> search(String query , int topK , float threshold );
}
