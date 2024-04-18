package com.galaxy.galaxy_drive.model.dto.minio;

import lombok.Value;

@Value
public class FolderDto {
    String name;
    String path;
    String parentFolderPath;
}
