package com.galaxy.galaxy_drive.model.dto.minio;

import lombok.Value;

import java.time.LocalDate;

@Value
public class MinioFileDto {
    String name;
    String path;
    String size;
    LocalDate lastModified;
    String parentFolderPath;
    String type;
}
