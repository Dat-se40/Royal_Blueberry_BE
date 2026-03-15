package com.example.Royal_Blueberry.entity.merriam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MWHeadwordInfo {
    @JsonProperty("hw")
    private String headword;

    @JsonProperty("prs")
    private List<MWPronunciation> pronunciations;
}
