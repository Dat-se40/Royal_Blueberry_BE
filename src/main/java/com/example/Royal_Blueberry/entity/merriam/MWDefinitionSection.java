package com.example.Royal_Blueberry.entity.merriam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MWDefinitionSection {
    // sseq quá phức tạp để map đầy đủ
    // Dùng JsonNode để xử lý linh hoạt
    @JsonProperty("sseq")
    private List<List<List<JsonNode>>> sseq;

    @JsonProperty("vd")
    private String verbDivider;
}
