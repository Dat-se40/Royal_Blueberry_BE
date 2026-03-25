package com.example.Royal_Blueberry.dto;

import com.mongodb.internal.connection.Time;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageDto {
    private String id ;
    private String name ;
    private String category;
    private String level ;
    private String description ;
    private int totalWords;
    private LocalDateTime updateAt ;
}

