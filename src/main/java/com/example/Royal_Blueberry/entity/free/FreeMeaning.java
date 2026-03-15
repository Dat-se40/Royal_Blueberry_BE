package com.example.Royal_Blueberry.entity.free;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FreeMeaning {
    public String partOfSpeech ;
    public List<FreeDefinition> definitions ;
    public List<String> synonyms ;
    public List<String> antonyms ;
}
