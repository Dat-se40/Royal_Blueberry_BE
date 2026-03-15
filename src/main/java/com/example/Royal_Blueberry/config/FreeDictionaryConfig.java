package com.example.Royal_Blueberry.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "free.dictionary")
@Data
public class FreeDictionaryConfig {
    private String uri ;
}
