package com.example.Royal_Blueberry.mapper;

import com.example.Royal_Blueberry.dto.PackageDto;
import com.example.Royal_Blueberry.entity.Package;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackageMapperTest {

    @Test
    void mapToPackageDtoCopiesAllFields() {
        LocalDateTime updateAt = LocalDateTime.of(2026, 6, 9, 10, 30);
        Package entity = new Package(
                "pkg-1",
                "Business English",
                "vocabulary",
                "intermediate",
                42,
                "Common office words",
                updateAt
        );

        PackageDto dto = PackageMapper.mapToPackageDto(entity);

        assertEquals("pkg-1", dto.getId());
        assertEquals("Business English", dto.getName());
        assertEquals("vocabulary", dto.getCategory());
        assertEquals("intermediate", dto.getLevel());
        assertEquals("Common office words", dto.getDescription());
        assertEquals(42, dto.getTotalWords());
        assertEquals(updateAt, dto.getUpdateAt());
    }

    @Test
    void mapToPackageCopiesAllFields() {
        LocalDateTime updateAt = LocalDateTime.of(2026, 6, 9, 11, 45);
        PackageDto dto = new PackageDto(
                "pkg-2",
                "Travel",
                "topic",
                "beginner",
                "Travel vocabulary",
                10,
                updateAt
        );

        Package entity = PackageMapper.mapToPackage(dto);

        assertEquals("pkg-2", entity.getId());
        assertEquals("Travel", entity.getName());
        assertEquals("topic", entity.getCategory());
        assertEquals("beginner", entity.getLevel());
        assertEquals("Travel vocabulary", entity.getDescription());
        assertEquals(10, entity.getTotalWords());
        assertEquals(updateAt, entity.getUpdateAt());
    }
}
