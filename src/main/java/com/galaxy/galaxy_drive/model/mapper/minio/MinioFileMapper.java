package com.galaxy.galaxy_drive.model.mapper.minio;

import com.galaxy.galaxy_drive.model.dto.minio.MinioFileDto;
import com.galaxy.galaxy_drive.model.mapper.Mapper;
import com.galaxy.galaxy_drive.util.FileUtil;
import com.galaxy.galaxy_drive.util.FolderUtil;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;

@Component
public class MinioFileMapper implements Mapper<Item, MinioFileDto> {

    @Override
    public MinioFileDto map(Item object) {
        return new MinioFileDto(
                FileUtil.getFileNameWithType(object.objectName()),
                object.objectName(),
                FileUtil.getSize(object.size()),
                object.lastModified().toLocalDate(),
                FolderUtil.getParentFolderPath(object.objectName()),
                FileUtil.getFileType(object.objectName())
        );
    }
}
