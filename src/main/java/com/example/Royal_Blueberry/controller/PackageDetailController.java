package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.PackageDetailDto;
import com.example.Royal_Blueberry.dto.WordEntryDto;
import com.example.Royal_Blueberry.service.PackageDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/packages/details")
@SecurityRequirement(name = com.example.Royal_Blueberry.config.OpenApiConfig.SECURITY_SCHEME_NAME)
@Tag(name = "Package Details", description = "Protected endpoints for package content and words")
public class PackageDetailController {
    private final PackageDetailService packageDetailService ;

    @Operation(
            summary = "Create package detail",
            description = "Creates the detail record for a package and initializes its word list."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Package detail created",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PackageDetailDto.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping("{id}")
    public ResponseEntity<PackageDetailDto> create(@PathVariable("id") String packageID,
                                                   @RequestBody PackageDetailDto dto )
    {
        return new ResponseEntity<>(packageDetailService.createPackageDetail(packageID,dto)
                                    ,HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get all package details",
            description = "Returns every package detail document currently stored."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Package details returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    public ResponseEntity<List<PackageDetailDto>> getAll()
    {
        return new ResponseEntity<>(packageDetailService.getAllDetails(),HttpStatus.OK);
    }

    @Operation(
            summary = "Get package detail by package ID",
            description = "Returns the detail document associated with a package."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Package detail found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PackageDetailDto.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("{packageId}")
    public ResponseEntity<PackageDetailDto> getDetailsByPackageId(@PathVariable("packageId") String packageID)
    {
        return  new ResponseEntity<>(packageDetailService.getDetailByPackageId(packageID),HttpStatus.OK);
    }

    @Operation(
            summary = "Add a new word to a package",
            description = "Appends a word entry to the package detail identified by package ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Word added to package",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PackageDetailDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "507f1f77bcf86cd799439030",
                                      "packageId": "507f1f77bcf86cd799439011",
                                      "totalWords": 11,
                                      "words": [
                                        {
                                          "word": "hello",
                                          "phonetic": "/huh-loh/",
                                          "partOfSpeech": "interjection",
                                          "definition": "used as a greeting",
                                          "example": "Hello, how are you?"
                                        }
                                      ]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping("{packageId}/new-word")
    public ResponseEntity<PackageDetailDto> addNewWord(@PathVariable("packageId") String packageId ,
                                                       @RequestBody WordEntryDto newWord)
    {
        return new ResponseEntity<>(packageDetailService.addWord(packageId,newWord),HttpStatus.OK);
    }
}
