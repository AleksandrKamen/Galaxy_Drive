package com.galaxy.galaxy_drive.model.mapper.minio;

import com.galaxy.galaxy_drive.model.dto.minio.FileDto;
import com.galaxy.galaxy_drive.model.dto.minio.FolderDto;
import io.minio.messages.Item;
import org.mapstruct.Mapper;
import java.time.format.DateTimeFormatter;
import static com.galaxy.galaxy_drive.util.FileStorageUtil.*;

@Mapper(componentModel = "spring")
public abstract class FileMapper {

    public FileDto itemToFileDto(Item object){
        return new FileDto(
                getFileName(object.objectName(), true),
                object.objectName(),
                getFileSize(object.size()),
                object.lastModified().format(DateTimeFormatter.ofPattern("dd-MM-yyy")),
                getParentFolderPath(object.objectName()),
                getFileType(object.objectName()));
    }

    public FolderDto stringToFolderDto(String folderPath){
        return new FolderDto(
                getNameFolder(folderPath),
                folderPath.substring(0, folderPath.length()-1),
                getParentFolderPath(folderPath)
        );
    }
}
