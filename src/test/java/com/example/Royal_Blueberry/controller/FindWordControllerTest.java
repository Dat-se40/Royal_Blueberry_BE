package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.WordDetailDto;
import com.example.Royal_Blueberry.service.FindWordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindWordControllerTest {

    @Mock
    private FindWordService findWordService;

    @InjectMocks
    private FindWordController controller;

    @Test
    void findWordTrimsInputAndReturnsServiceResult() {
        WordDetailDto response = WordDetailDto.builder()
                .word("hello")
                .meanings(List.of())
                .build();
        when(findWordService.findWord("hello")).thenReturn(response);

        ResponseEntity<WordDetailDto> result = controller.findWord(" hello ");

        verify(findWordService).findWord("hello");
        assertEquals(200, result.getStatusCode().value());
        assertEquals("hello", result.getBody().getWord());
    }
}
