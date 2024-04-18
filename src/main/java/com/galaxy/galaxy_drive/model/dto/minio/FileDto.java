package com.galaxy.galaxy_drive.model.dto.minio;

import lombok.Value;

@Value
public class FileDto {
    String name;
    String path;
    String size;
    String lastModified;
    String parentFolderPath;
    String type;
}
