package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.PackageDto;
import com.example.Royal_Blueberry.service.PackageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageControllerTest {

    @Mock
    private PackageService packageService;

    @InjectMocks
    private PackageController controller;

    @Test
    void createPackageReturnsCreatedResponse() {
        PackageDto dto = pkg("pkg-1", "Business", "vocabulary", "advanced", 10);
        when(packageService.createPackage(dto)).thenReturn(dto);

        ResponseEntity<PackageDto> response = controller.createPackage(dto);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("pkg-1", response.getBody().getId());
    }

    @Test
    void getAllPackagesAppliesFiltersInController() {
        when(packageService.getAllPackages()).thenReturn(List.of(
                pkg("pkg-1", "Basic", "vocabulary", "beginner", 5),
                pkg("pkg-2", "Business", "business-vocab", "advanced", 30),
                pkg("pkg-3", "Travel", "topic", "beginner", 50)
        ));

        ResponseEntity<List<PackageDto>> response = controller.getAllPackages(
                "vocab",
                null,
                "like",
                10,
                40
        );

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals("pkg-2", response.getBody().get(0).getId());
    }

    @Test
    void getAllPackagesReturnsEmptyForUnknownOperator() {
        when(packageService.getAllPackages()).thenReturn(List.of(
                pkg("pkg-1", "Basic", "vocabulary", "beginner", 5)
        ));

        ResponseEntity<List<PackageDto>> response = controller.getAllPackages(
                "vocabulary",
                null,
                "unknown",
                null,
                null
        );

        assertEquals(0, response.getBody().size());
    }

    @Test
    void getPackageReturnsServiceResult() {
        when(packageService.getPackage("pkg-1")).thenReturn(pkg("pkg-1", "Basic", "vocabulary",
                "beginner", 5));

        ResponseEntity<PackageDto> response = controller.getPackage("pkg-1");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Basic", response.getBody().getName());
    }

    private PackageDto pkg(String id, String name, String category, String level, int totalWords) {
        return new PackageDto(id, name, category, level, "desc", totalWords,
                LocalDateTime.of(2026, 6, 9, 10, 0));
    }
}
