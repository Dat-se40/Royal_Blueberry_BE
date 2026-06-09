package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.dto.PackageDetailDto;
import com.example.Royal_Blueberry.dto.WordEntryDto;
import com.example.Royal_Blueberry.entity.PackageDetail;
import com.example.Royal_Blueberry.repository.PackageDetailRepository;
import com.example.Royal_Blueberry.repository.PackageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageDetailServiceImplTest {

    @Mock
    private PackageRepository repository;

    @Mock
    private PackageDetailRepository packageDetailRepository;

    @InjectMocks
    private PackageDetailServiceImpl service;

    @Test
    void createPackageDetailSavesDetailAndUpdatesPackageWordCount() {
        com.example.Royal_Blueberry.entity.Package targetPackage =
                new com.example.Royal_Blueberry.entity.Package();
        targetPackage.setId("pkg-1");
        when(repository.findById("pkg-1")).thenReturn(Optional.of(targetPackage));

        PackageDetailDto input = PackageDetailDto.builder()
                .words(List.of(
                        WordEntryDto.builder().word("hello").build(),
                        WordEntryDto.builder().word("world").build()
                ))
                .build();

        when(packageDetailRepository.save(any(PackageDetail.class))).thenAnswer(invocation -> {
            PackageDetail entity = invocation.getArgument(0);
            return PackageDetail.builder()
                    .id("detail-1")
                    .packageId(entity.getPackageId())
                    .words(entity.getWords())
                    .build();
        });

        PackageDetailDto result = service.createPackageDetail("pkg-1", input);

        assertEquals("detail-1", result.getId());
        assertEquals("pkg-1", result.getPackageId());
        assertEquals(2, result.getWords().size());

        ArgumentCaptor<com.example.Royal_Blueberry.entity.Package> packageCaptor =
                ArgumentCaptor.forClass(com.example.Royal_Blueberry.entity.Package.class);
        verify(repository).save(packageCaptor.capture());
        assertEquals(2, packageCaptor.getValue().getTotalWords());
    }

    @Test
    void createPackageDetailThrowsWhenPackageDoesNotExist() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.createPackageDetail("missing", PackageDetailDto.builder().build()));

        assertEquals("Package ID missing is not found", exception.getMessage());
    }

    @Test
    void addWordAppendsToExistingDetailAndPersistsIt() {
        PackageDetail existing = PackageDetail.builder()
                .id("detail-1")
                .packageId("pkg-1")
                .words(new ArrayList<>(List.of(
                        toWordEntity("hello")
                )))
                .build();
        when(packageDetailRepository.findAll()).thenReturn(List.of(existing));
        when(packageDetailRepository.save(any(PackageDetail.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        WordEntryDto newWord = WordEntryDto.builder()
                .word("world")
                .definition("earth")
                .build();

        PackageDetailDto result = service.addWord("pkg-1", newWord);

        assertEquals(2, result.getWords().size());
        assertEquals("world", result.getWords().get(1).getWord());
        verify(packageDetailRepository).save(any(PackageDetail.class));
    }

    @Test
    void getDetailByPackageIdReturnsMatchingDetail() {
        when(packageDetailRepository.findAll()).thenReturn(List.of(
                PackageDetail.builder().id("detail-1").packageId("pkg-a").words(List.of()).build(),
                PackageDetail.builder().id("detail-2").packageId("pkg-b")
                        .words(List.of(toWordEntity("hello"))).build()
        ));

        PackageDetailDto result = service.getDetailByPackageId("pkg-b");

        assertEquals("detail-2", result.getId());
        assertEquals("pkg-b", result.getPackageId());
        assertEquals(1, result.getWords().size());
    }

    @Test
    void getDetailByPackageIdThrowsWhenNotFound() {
        when(packageDetailRepository.findAll()).thenReturn(List.of());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.getDetailByPackageId("missing"));

        assertEquals("Package ID is missingis not found", exception.getMessage());
    }

    @Test
    void getAllDetailsMapsAllRecords() {
        when(packageDetailRepository.findAll()).thenReturn(List.of(
                PackageDetail.builder().id("detail-1").packageId("pkg-1")
                        .words(List.of(toWordEntity("hello"))).build(),
                PackageDetail.builder().id("detail-2").packageId("pkg-2")
                        .words(List.of()).build()
        ));

        List<PackageDetailDto> results = service.getAllDetails();

        assertEquals(2, results.size());
        assertEquals("pkg-1", results.get(0).getPackageId());
        assertEquals(1, results.get(0).getTotalWords());
        assertEquals("pkg-2", results.get(1).getPackageId());
    }

    private com.example.Royal_Blueberry.entity.WordEntry toWordEntity(String word) {
        com.example.Royal_Blueberry.entity.WordEntry entry =
                new com.example.Royal_Blueberry.entity.WordEntry();
        entry.setWord(word);
        return entry;
    }
}
