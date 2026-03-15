package com.example.Royal_Blueberry.entity.merriam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MWMeta {
    @JsonProperty("id")
    private String id;

    @JsonProperty("stems")
    private List<String> stems;

    @JsonProperty("offensive")
    private boolean offensive;
}
