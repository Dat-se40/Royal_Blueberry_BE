package com.example.Royal_Blueberry.entity.merriam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MWInflection {
    @JsonProperty("il")
    private String label;

    @JsonProperty("if")
    private String inflectedForm;

    @JsonProperty("prs")
    private List<MWPronunciation> pronunciations;
}
