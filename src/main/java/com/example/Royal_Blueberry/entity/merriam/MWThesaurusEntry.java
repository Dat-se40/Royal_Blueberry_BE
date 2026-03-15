package com.example.Royal_Blueberry.entity.merriam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MWThesaurusEntry {
    @JsonProperty("meta")
    private MWMeta meta;

    @JsonProperty("hwi")
    private MWHeadwordInfo hwi;

    @JsonProperty("fl")
    private String functionalLabel;

    @JsonProperty("def")
    private List<MWThesaurusDefinition> definitions;

    @JsonProperty("shortdef")
    private List<String> shortDefinitions;
}
