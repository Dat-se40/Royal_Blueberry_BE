package com.example.Royal_Blueberry.service.impl;

import com.example.Royal_Blueberry.dto.PackageDetailDto;
import com.example.Royal_Blueberry.dto.WordEntryDto;
import com.example.Royal_Blueberry.entity.PackageDetail;
import com.example.Royal_Blueberry.mapper.PackageDetailMapper;
import com.example.Royal_Blueberry.repository.PackageDetailRepository;
import com.example.Royal_Blueberry.repository.PackageRepository;
import com.example.Royal_Blueberry.service.PackageDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageDetailServiceImpl implements PackageDetailService {
    private final PackageRepository repository;
    private final PackageDetailRepository packageDetailRepository;
    @Override
    public PackageDetailDto createPackageDetail(String packageID, PackageDetailDto packageDetailDto) {
        log.info("[PackageDetailService] Creating detail for package - packageId={}", packageID);
        var targetPackage = repository.findById(packageID).orElseThrow(() -> {
                log.warn("[PackageDetailService] Package not found - packageId={}", packageID);
                return new RuntimeException("Package ID " + packageID + " is not found");
        });

        packageDetailDto.setPackageId(packageID);
        var savedPackageDetail = packageDetailRepository.save(
                PackageDetailMapper.toEntity(packageDetailDto)
        );

        // Sync totalWords về Package
        targetPackage.setTotalWords(savedPackageDetail.getWords().size());
        targetPackage.setUpdateAt(LocalDateTime.now());
        repository.save(targetPackage);

        log.info("[PackageDetailService] Detail created - detailId={}, words={}",
                savedPackageDetail.getId(), savedPackageDetail.getWords().size());
        return PackageDetailMapper.toDto(savedPackageDetail);
    }
    @Override
    public PackageDetailDto addWord(String packageId, WordEntryDto wordDetailDto) {
        log.info("[PackageDetailService] Adding word to package - packageId={}, word='{}'",
                packageId, wordDetailDto.getWord());
        var targetDetailDto = getDetailByPackageId(packageId);
        targetDetailDto.getWords().add(wordDetailDto);
        var savedDetail = packageDetailRepository.save(PackageDetailMapper.toEntity(targetDetailDto));
        log.info("[PackageDetailService] Word added - totalWords={}", savedDetail.getWords().size());
        return PackageDetailMapper.toDto(savedDetail);
    }

    @Override
    public PackageDetailDto deleteWord(String packageId, String wordName) {
        return null;
    }

    @Override
    public PackageDetailDto getDetailByPackageId(String packageId) {
        log.debug("[PackageDetailService] Fetching detail by packageId={}", packageId);
        List<PackageDetail> packageDetailList = packageDetailRepository.findAll();
        var result = packageDetailList.stream().filter(packageDetail -> packageDetail.getPackageId().equals(packageId))
                .findFirst().orElseThrow(() -> {
                    log.warn("[PackageDetailService] Detail not found - packageId={}", packageId);
                    return new RuntimeException("Package ID is " + packageId + "is not found");
                });
        return PackageDetailMapper.toDto(result);
    }

    @Override
    public List<PackageDetailDto> getAllDetails() {
        log.debug("[PackageDetailService] Fetching all package details");
        List<PackageDetail> packageDetailList = packageDetailRepository.findAll();
        List<PackageDetailDto> dtos = new ArrayList<>();
        packageDetailList.forEach(packageDetail -> dtos.add(PackageDetailMapper.toDto(packageDetail)));
        log.debug("[PackageDetailService] Found {} package details", dtos.size());
        return dtos;
    }

}

