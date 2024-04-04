package com.galaxy.galaxy_drive.model.mapper.minio;

import com.galaxy.galaxy_drive.model.dto.minio.MinioFileDto;
import com.galaxy.galaxy_drive.model.mapper.Mapper;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;

@Component
public class MinioFileMapper implements Mapper<Item, MinioFileDto> {
    @Override
    public MinioFileDto map(Item object) {
        return new MinioFileDto(
                getFileName(object.objectName()),
                object.objectName(),
                getSize(object.size()),
                object.lastModified().toLocalDate(),
                getParentFolderPath(object.objectName()),
                getType(object.objectName())
        );
    }

    private String getFileName(String fullPath) {
        return fullPath.substring(fullPath.lastIndexOf("/") + 1);
    }

    private String getParentFolderPath(String fullPath) {
        return fullPath.substring(0, fullPath.lastIndexOf("/"));
    }

    private String getType(String fullPath) {
        return fullPath.substring(fullPath.lastIndexOf(".") + 1);
    }

    private String getSize(Long size){
         if (size > 1073741824){
             return size/1073741824 + " GB";
         }
         if (size > 1048576){
             return size/1048576 + " MB";
         }
         if (size > 1024){
             return size/1024 + " KB";
         }
         return size + " B";
    }
}
