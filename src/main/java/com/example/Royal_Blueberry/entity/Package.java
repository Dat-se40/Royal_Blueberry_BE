package com.example.Royal_Blueberry.entity;

import com.mongodb.internal.connection.Time;
import jakarta.annotation.Generated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Available Packages")
public class Package {
    @Id
    private String id ;
    private String name ;
    private String category;
    private String level ;
    private int totalWords;
    private String description ;
    private LocalDateTime updateAt ;
}
