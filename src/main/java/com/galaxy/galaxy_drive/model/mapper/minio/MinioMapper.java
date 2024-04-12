package com.galaxy.galaxy_drive.model.mapper.minio;

import com.galaxy.galaxy_drive.model.dto.minio.MinioFileDto;
import com.galaxy.galaxy_drive.model.dto.minio.MinioFolderDto;
import com.galaxy.galaxy_drive.util.FileUtil;
import com.galaxy.galaxy_drive.util.FolderUtil;
import io.minio.messages.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class MinioMapper {

    public MinioFileDto itemToMinioFileDto(Item object){
        return new MinioFileDto(
                FileUtil.getFileNameWithType(object.objectName()),
                object.objectName(),
                FileUtil.getSize(object.size()),
                object.lastModified().toLocalDate(),
                FolderUtil.getParentFolderPath(object.objectName()),
                FileUtil.getFileType(object.objectName()));
    }

    public MinioFolderDto itemToMinioFolderDto(String folderPath){
        return new MinioFolderDto(
                FolderUtil.getNameFolder(folderPath),
                folderPath.substring(0, folderPath.length()-1),
                FolderUtil.getParentFolderPath(folderPath)
        );

    }

}
