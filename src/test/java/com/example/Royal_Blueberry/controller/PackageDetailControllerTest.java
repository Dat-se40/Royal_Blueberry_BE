package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.PackageDetailDto;
import com.example.Royal_Blueberry.dto.WordEntryDto;
import com.example.Royal_Blueberry.service.PackageDetailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageDetailControllerTest {

    @Mock
    private PackageDetailService packageDetailService;

    @InjectMocks
    private PackageDetailController controller;

    @Test
    void createReturnsCreatedDetail() {
        PackageDetailDto dto = detail("detail-1", "pkg-1", "hello");
        when(packageDetailService.createPackageDetail("pkg-1", dto)).thenReturn(dto);

        ResponseEntity<PackageDetailDto> response = controller.create("pkg-1", dto);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("detail-1", response.getBody().getId());
    }

    @Test
    void getAllReturnsAllDetails() {
        when(packageDetailService.getAllDetails()).thenReturn(List.of(
                detail("detail-1", "pkg-1", "hello"),
                detail("detail-2", "pkg-2", "world")
        ));

        ResponseEntity<List<PackageDetailDto>> response = controller.getAll();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getDetailsByPackageIdReturnsDetail() {
        when(packageDetailService.getDetailByPackageId("pkg-1")).thenReturn(detail("detail-1",
                "pkg-1", "hello"));

        ResponseEntity<PackageDetailDto> response = controller.getDetailsByPackageId("pkg-1");

        assertEquals("pkg-1", response.getBody().getPackageId());
    }

    @Test
    void addNewWordReturnsUpdatedDetail() {
        WordEntryDto word = WordEntryDto.builder().word("world").build();
        when(packageDetailService.addWord("pkg-1", word)).thenReturn(detail("detail-1", "pkg-1",
                "hello", "world"));

        ResponseEntity<PackageDetailDto> response = controller.addNewWord("pkg-1", word);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().getWords().size());
    }

    private PackageDetailDto detail(String id, String packageId, String... words) {
        return PackageDetailDto.builder()
                .id(id)
                .packageId(packageId)
                .words(java.util.Arrays.stream(words)
                        .map(word -> WordEntryDto.builder().word(word).build())
                        .toList())
                .totalWords(words.length)
                .build();
    }
}
