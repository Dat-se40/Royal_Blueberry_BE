package com.example.Royal_Blueberry.entity;

import jdk.jshell.Snippet;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "PackageDetail")
@Getter
@Setter
@Builder
// Showing words is included in this package
public class PackageDetail {
    @Id
    private String id;
    private String packageId;
    private List<WordEntry> words ;
}
