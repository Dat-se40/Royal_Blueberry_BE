package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.WordDetailDto;
import com.example.Royal_Blueberry.entity.EmbedWordVector;
import com.example.Royal_Blueberry.service.EmbedWordService;
import com.example.Royal_Blueberry.service.FindWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/searching")
@Slf4j
@Tag(name = "Dictionary Search", description = "Public endpoints for dictionary lookup")
public class FindWordController {
    private final FindWordService findWordService ;
    // ─────────────────────────────────────────────────────── BASIC SEARCH ──

    /**
     * Get full word details by word name
     */
    @Operation(
            summary = "Get word details",
            description = """
                    Retrieves complete information about a word including:
                    - Phonetic pronunciation
                    - Multiple meanings and definitions
                    - Examples and synonyms/antonyms
                    - Audio pronunciations (US & UK)
                    
                    This is the basic endpoint for dictionary lookup.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Word found successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = WordDetailDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "word": "hello",
                                      "phonetic": "/həˈloʊ/",
                                      "audioUs": "https://...",
                                      "audioUk": "https://...",
                                      "meanings": [
                                        {
                                          "meaningIndex": 0,
                                          "partOfSpeech": "noun",
                                          "definitions": [
                                            {
                                              "definitionIndex": 0,
                                              "definition": "a polite expression of greeting",
                                              "example": "She said hello to her friend",
                                              "synonyms": ["greeting", "salutation"],
                                              "antonyms": []
                                            }
                                          ],
                                          "synonyms": ["hi", "hey"],
                                          "antonyms": ["goodbye"]
                                        }
                                      ]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid word (empty or null)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-03-24T10:00:00.000+00:00",
                                      "status": 400,
                                      "path": "/api/searching/get-detail/",
                                      "error": "Bad Request",
                                      "message": "Word cannot be empty"
                                    }
                                    """))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Word not found in dictionary",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-03-24T10:00:00.000+00:00",
                                      "status": 404,
                                      "path": "/api/searching/get-detail/xyz",
                                      "error": "Not Found",
                                      "message": "Word not found: xyz"
                                    }
                                    """))
            )
    })
    @GetMapping("get-detail/{word}")
    public ResponseEntity<WordDetailDto> findWord(@PathVariable("word") String word)
    {
        log.info("have a request form client");
        WordDetailDto dto = findWordService.findWord(word.trim());
        return ResponseEntity.ok(dto);
    }
}
