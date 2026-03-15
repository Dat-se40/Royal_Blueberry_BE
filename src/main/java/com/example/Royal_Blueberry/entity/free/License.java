package com.example.Royal_Blueberry.entity.free;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class License
{
    public String name;
    public String url;
}
