package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.PackageDto;
import com.example.Royal_Blueberry.service.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/api/packages")
@Slf4j
@SecurityRequirement(name = com.example.Royal_Blueberry.config.OpenApiConfig.SECURITY_SCHEME_NAME)
@Tag(name = "Packages", description = "Protected endpoints for managing vocabulary packages")
public class PackageController
{
    private final PackageService packageService ;
    @Operation(
            summary = "Create new package",
            description = """
                    Creates a new word package (collection).
                    
                    **Required fields:**
                    - `name`: Package name (e.g., "Business English")
                    - `category`: Category (e.g., "vocabulary", "phrasal verbs")
                    - `level`: Difficulty level (beginner, intermediate, advanced)
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Package created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PackageDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "507f1f77bcf86cd799439011",
                                      "name": "Business English",
                                      "category": "vocabulary",
                                      "level": "intermediate",
                                      "totalWords": 0,
                                      "updateAt": "2026-03-24T10:00:00Z"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid package data (missing field, invalid format)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-03-24T10:00:00.000+00:00",
                                      "status": 400,
                                      "path": "/api/packages",
                                      "error": "Bad Request",
                                      "message": "name must not be blank"
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping
    public ResponseEntity<PackageDto> createPackage(@RequestBody PackageDto packageDto)
    {
        PackageDto createdPackage = packageService.createPackage(packageDto);
        return new ResponseEntity<>(createdPackage, HttpStatus.CREATED);
    }
    // ────────────────────────────────────────────────── GET ALL PACKAGES ──

    @Operation(
            summary = "Get all packages with filtering",
            description = """
                    Retrieves all packages with optional filtering by:
                    - **category**: Filter by category name
                    - **op**: Operator for category filter (eq, like, regex) - default: eq
                    - **level**: Filter by difficulty level
                    - **minWords**: Minimum word count
                    - **maxWords**: Maximum word count
                    
                    **Examples:**
                    - Get all packages: `?`
                    - Contains "vocab": `?category=vocab&op=like`
                    - Beginner level, 10-50 words: `?level=beginner&minWords=10&maxWords=50`
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of packages",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "id": "507f1f77bcf86cd799439011",
                                        "name": "Basic English",
                                        "category": "vocabulary",
                                        "level": "beginner",
                                        "totalWords": 50,
                                        "updateAt": "2026-03-24T10:00:00Z"
                                      },
                                      {
                                        "id": "507f1f77bcf86cd799439012",
                                        "name": "Advanced Vocabulary",
                                        "category": "vocabulary",
                                        "level": "advanced",
                                        "totalWords": 100,
                                        "updateAt": "2026-03-24T10:05:00Z"
                                      }
                                    ]
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid operator or parameters",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    public ResponseEntity<List<PackageDto>> getAllPackages(
            @io.swagger.v3.oas.annotations.Parameter(
                    name = "category",
                    description = "Filter by category (e.g., vocabulary, phrasal-verbs)",
                    example = "vocabulary",
                    required = false
            )
            @RequestParam(value = "category", required = false) String category,

            @io.swagger.v3.oas.annotations.Parameter(
                    name = "level",
                    description = "Filter by level (beginner, intermediate, advanced)",
                    example = "beginner",
                    required = false
            )
            @RequestParam(value = "level", required = false) String level,

            @io.swagger.v3.oas.annotations.Parameter(
                    name = "op",
                    description = "Operator for category filter (eq, like, regex)",
                    example = "like",
                    required = false
            )
            @RequestParam(value = "op", defaultValue = "eq") String operator,

            @io.swagger.v3.oas.annotations.Parameter(
                    name = "minWords",
                    description = "Minimum word count",
                    example = "10",
                    required = false
            )
            @RequestParam(value = "minWords", required = false) Integer minWords,

            @io.swagger.v3.oas.annotations.Parameter(
                    name = "maxWords",
                    description = "Maximum word count",
                    example = "100",
                    required = false
            )
            @RequestParam(value = "maxWords", required = false) Integer maxWords) {

        log.info("[Package] GET all packages | category={}, level={}, op={}, minWords={}, maxWords={}",
                category, level, operator, minWords, maxWords);

        List<PackageDto> allPackages = packageService.getAllPackages();

        if (category != null && !category.isEmpty()) {
            allPackages = filterByString(allPackages, "category", category, operator);
        }

        if (level != null && !level.isEmpty()) {
            allPackages = filterByString(allPackages, "level", level, "eq");
        }

        if (minWords != null) {
            allPackages = allPackages.stream()
                    .filter(pkg -> pkg.getTotalWords() >= minWords)
                    .collect(Collectors.toList());
        }

        if (maxWords != null) {
            allPackages = allPackages.stream()
                    .filter(pkg -> pkg.getTotalWords() <= maxWords)
                    .collect(Collectors.toList());
        }

        return new ResponseEntity<>(allPackages, HttpStatus.OK);
    }
    @Operation(
            summary = "Get package by ID",
            description = "Retrieves a single package by its MongoDB ObjectId"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Package found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PackageDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Package not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-03-24T10:00:00.000+00:00",
                                      "status": 404,
                                      "path": "/api/packages/507f1f77bcf86cd799439099",
                                      "error": "Not Found",
                                      "message": "Package not found"
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("{id}")
    public ResponseEntity<PackageDto> getPackage(@PathVariable String id)
    {
        PackageDto foundPackage = packageService.getPackage(id);
        return ResponseEntity.ok(foundPackage);
    }
    /*
    * Support method
    * */
    private List<PackageDto> filterByString(List<PackageDto> packages, String field, String value, String operator) {
        return packages.stream()
                .filter(pkg -> {
                    String fieldValue = getFieldValue(pkg, field);
                    return matchesCriteria(fieldValue, value, operator);
                })
                .collect(Collectors.toList());
    }
    private String getFieldValue(PackageDto pkg, String field) {
        return switch (field.toLowerCase()) {
            case "name" -> pkg.getName();
            case "category" -> pkg.getCategory();
            case "level" -> pkg.getLevel();
            default -> null;
        };
    }
    private boolean matchesCriteria(String fieldValue, String searchValue, String operator) {
        if (fieldValue == null) {
            return "NOT_EXISTS".equalsIgnoreCase(operator);
        }

        return switch (operator.toUpperCase()) {
            case "EQ" -> fieldValue.equalsIgnoreCase(searchValue);
            case "NE" -> !fieldValue.equalsIgnoreCase(searchValue);
            case "LIKE" -> fieldValue.toLowerCase().contains(searchValue.toLowerCase());
            case "NOT_LIKE" -> !fieldValue.toLowerCase().contains(searchValue.toLowerCase());
            case "REGEX" -> {
                try {
                    yield java.util.regex.Pattern.compile(searchValue)
                            .matcher(fieldValue).find();
                } catch (Exception e) {
                    log.warn("[Package] Invalid regex: {}", searchValue);
                    yield false;
                }
            }
            default -> {
                log.warn("[Package] Unknown operator: {}", operator);
                yield false;
            }
        };
    }
}
