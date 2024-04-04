package com.galaxy.galaxy_drive.model.mapper.minio;

import com.galaxy.galaxy_drive.model.dto.minio.MinioFolderDto;
import com.galaxy.galaxy_drive.model.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class MinioFolderMapper implements Mapper<String, MinioFolderDto> {
    @Override
    public MinioFolderDto map(String folderPath) {
        return new MinioFolderDto(
                getNameFolder(folderPath),
                folderPath.substring(0, folderPath.length()-1),
                getParentFolderPath(folderPath)
        );
    }

    private String getNameFolder(String fullPath){
        var substring = fullPath.substring(0, fullPath.length() - 1);
        return substring.substring(substring.lastIndexOf("/")+1);
    }

    private String getParentFolderPath(String fullPath){
        var substring = fullPath.substring(0, fullPath.length() - 1);
        return substring.substring(0, substring.lastIndexOf("/"));

    }
}
