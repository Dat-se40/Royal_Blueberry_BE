package com.example.Royal_Blueberry.controller;

import com.example.Royal_Blueberry.dto.PackageDetailDto;
import com.example.Royal_Blueberry.dto.PackageDto;
import com.example.Royal_Blueberry.dto.WordEntryDto;
import com.example.Royal_Blueberry.mapper.PackageDetailMapper;
import com.example.Royal_Blueberry.service.PackageDetailService;
import com.example.Royal_Blueberry.service.PackageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/packages/details")
public class PackageDetailController {
    private final PackageDetailService packageDetailService ;
    @PostMapping("{id}")
    public ResponseEntity<PackageDetailDto> create(@PathVariable("id") String packageID,
                                                   @RequestBody PackageDetailDto dto )
    {
        return new ResponseEntity<>(packageDetailService.createPackageDetail(packageID,dto)
                                    ,HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<PackageDetailDto>> getAll()
    {
        return new ResponseEntity<>(packageDetailService.getAllDetails(),HttpStatus.OK);
    }
    @GetMapping("{packageId}")
    public ResponseEntity<PackageDetailDto> getDetailsByPackageId(@PathVariable("packageId") String packageID)
    {
        return  new ResponseEntity<>(packageDetailService.getDetailByPackageId(packageID),HttpStatus.OK);
    }
    @PostMapping("{packageId}/new-word")
    public ResponseEntity<PackageDetailDto> addNewWord(@PathVariable("packageId") String packageId ,
                                                       @RequestBody WordEntryDto newWord)
    {
        return new ResponseEntity<>(packageDetailService.addWord(packageId,newWord),HttpStatus.OK);
    }
}
