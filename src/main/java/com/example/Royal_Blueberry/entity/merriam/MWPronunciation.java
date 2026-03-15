package com.example.Royal_Blueberry.entity.merriam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MWPronunciation {
    @JsonProperty("mw")
    private String mw;

    @JsonProperty("sound")
    private MWSound sound;
}
