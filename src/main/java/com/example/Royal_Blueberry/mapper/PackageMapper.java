package com.example.Royal_Blueberry.mapper;

import com.example.Royal_Blueberry.dto.PackageDto;
import com.example.Royal_Blueberry.entity.Package;

public class PackageMapper {
    public static PackageDto mapToPackageDto(Package _package)
    {
        return new PackageDto(_package.getId(), _package.getName(),
                _package.getCategory(), _package.getLevel(),
                _package.getTotalWords(), _package.getUpdateAt());
    }
    public static Package mapToPackage( PackageDto _package)
    {
        return new Package(_package.getId(), _package.getName(),
                _package.getCategory(), _package.getLevel(),
                _package.getTotalWords(), _package.getUpdateAt());

    }
}
