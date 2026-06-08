package com.example.Royal_Blueberry.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "merriam.webster")
@Data
public class MerriamWebsterConfig {
    private String dictKey ;
    private String thesaurusKey ;
    private String dictUri ;
    private String thesaurusUri;
}