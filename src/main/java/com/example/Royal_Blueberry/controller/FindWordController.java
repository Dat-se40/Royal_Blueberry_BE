package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.WordDetailDto;
import com.example.Royal_Blueberry.entity.EmbedWordVector;
import com.example.Royal_Blueberry.service.EmbedWordService;
import com.example.Royal_Blueberry.service.FindWordService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/searching")
public class FindWordController {
    private final FindWordService findWordService ;
    @GetMapping("get-detail/{word}")
    public ResponseEntity<WordDetailDto> findWord(@PathVariable("word") String word)
    {
        WordDetailDto dto = findWordService.findWord(word.trim());
        return ResponseEntity.ok(dto);
    }
}
