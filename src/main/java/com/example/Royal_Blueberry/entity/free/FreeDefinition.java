package com.example.Royal_Blueberry.entity.free;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FreeDefinition {
    String definition ;
    List<String> synonyms ;
    List<String> antonyms;
    String example ;
}
