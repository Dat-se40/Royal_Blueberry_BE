package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.dto.PackageDto;
import com.example.Royal_Blueberry.entity.Package;
import com.example.Royal_Blueberry.mapper.PackageMapper;
import com.example.Royal_Blueberry.repository.PackageRepository;
import com.example.Royal_Blueberry.service.FindWordService;
import com.example.Royal_Blueberry.service.PackageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class PackageServiceImpl implements PackageService {
    private final PackageRepository repository;
    @Override
    public PackageDto createPackage(PackageDto packageDto) {
        log.info("[PackageService] Creating package - name='{}', category='{}'",
                packageDto.getName(), packageDto.getCategory());
        Package _package = PackageMapper.mapToPackage(packageDto);
        _package.setUpdateAt(LocalDateTime.now());
        PackageDto result = PackageMapper.mapToPackageDto(repository.save(_package));
        log.info("[PackageService] Package saved - id={}", result.getId());
        return result;
    }

    @Override
    public PackageDto getPackage(String id) {
        log.debug("[PackageService] Fetching package - id={}", id);
        return PackageMapper.mapToPackageDto(repository.findById(id).get());
    }

    @Override
    public List<PackageDto> getAllPackages() {
        log.debug("[PackageService] Fetching all packages");
        var result = repository.findAll();
        List<PackageDto> packageDtos = new ArrayList<PackageDto>();
        result.forEach( r -> packageDtos.add(PackageMapper.mapToPackageDto(r)));
        log.debug("[PackageService] Found {} packages", packageDtos.size());
        return packageDtos;
    }
}
