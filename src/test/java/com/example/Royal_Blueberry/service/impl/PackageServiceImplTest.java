package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.dto.PackageDto;
import com.example.Royal_Blueberry.entity.Package;
import com.example.Royal_Blueberry.repository.PackageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageServiceImplTest {

    @Mock
    private PackageRepository repository;

    @InjectMocks
    private PackageServiceImpl service;

    @Test
    void createPackageSetsUpdateTimeAndReturnsMappedDto() {
        PackageDto input = new PackageDto(null, "Business", "vocabulary", "advanced",
                "Business terms", 12, null);
        when(repository.save(any(Package.class))).thenAnswer(invocation -> {
            Package saved = invocation.getArgument(0);
            saved.setId("pkg-1");
            return saved;
        });

        PackageDto result = service.createPackage(input);

        ArgumentCaptor<Package> captor = ArgumentCaptor.forClass(Package.class);
        verify(repository).save(captor.capture());
        Package savedEntity = captor.getValue();

        assertEquals("Business", savedEntity.getName());
        assertEquals("Business terms", savedEntity.getDescription());
        assertNotNull(savedEntity.getUpdateAt());

        assertEquals("pkg-1", result.getId());
        assertEquals("Business", result.getName());
        assertEquals("advanced", result.getLevel());
        assertEquals(12, result.getTotalWords());
        assertNotNull(result.getUpdateAt());
    }

    @Test
    void getPackageMapsRepositoryEntity() {
        LocalDateTime updateAt = LocalDateTime.of(2026, 6, 9, 14, 0);
        Package entity = new Package("pkg-2", "Travel", "topic", "beginner",
                8, "Travel basics", updateAt);
        when(repository.findById("pkg-2")).thenReturn(Optional.of(entity));

        PackageDto result = service.getPackage("pkg-2");

        assertEquals("pkg-2", result.getId());
        assertEquals("Travel", result.getName());
        assertEquals("Travel basics", result.getDescription());
        assertEquals(updateAt, result.getUpdateAt());
    }

    @Test
    void getAllPackagesMapsEveryEntity() {
        when(repository.findAll()).thenReturn(List.of(
                new Package("pkg-1", "Basic", "vocabulary", "beginner", 4, "Desc 1",
                        LocalDateTime.of(2026, 6, 9, 8, 0)),
                new Package("pkg-2", "Advanced", "topic", "advanced", 10, "Desc 2",
                        LocalDateTime.of(2026, 6, 9, 9, 0))
        ));

        List<PackageDto> results = service.getAllPackages();

        assertEquals(2, results.size());
        assertEquals("Basic", results.get(0).getName());
        assertEquals("Advanced", results.get(1).getName());
        assertEquals(10, results.get(1).getTotalWords());
    }
}
