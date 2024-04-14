package com.galaxy.galaxy_drive.service.minio;

import com.galaxy.galaxy_drive.exception.minio.*;
import com.galaxy.galaxy_drive.model.dto.minio.MinioFileDto;
import com.galaxy.galaxy_drive.model.dto.minio.MinioFolderDto;
import com.galaxy.galaxy_drive.model.mapper.minio.MinioMapper;
import com.galaxy.galaxy_drive.props.MinioProperties;
import com.galaxy.galaxy_drive.util.FileUtil;
import com.galaxy.galaxy_drive.util.FolderUtil;
import io.minio.*;
import io.minio.messages.Item;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinioService {
    MinioClient minioClient;
    MinioProperties minioProperties;
    MinioMapper minioMapper;
    MessageSource messageSource;
    static Long MAX_MEMORY = 1073741824L;

    public void delete(String fileName, String type) {
        switch (type) {
            case "file" -> removeObject(fileName);
            case "folder" -> getAllObjectInFolder(fileName, true).stream()
                    .forEach(it -> removeObject(it.objectName()));
        }
    }

    public void copy(String userFolder, String currentName, String copyName, String type) {
        if (!isFileFitOnDisk(userFolder, getObjectSize(currentName, type))) {
            throw new MemoryLlimitException(getErrorMessage("memory.limit"));
        }
        if (!isNameCorrect(copyName)){
            throw new IncorrectNameException(getErrorMessage("incorrect.name"));
        }

        switch (type) {
            case "file" -> copyFile(currentName, copyName);
            case "folder" -> copyFolder(currentName, copyName);
        }
    }

    public void rename(String currentName, String newName, String type) {
        if (!isNameCorrect(newName)){
            throw new IncorrectNameException(getErrorMessage("incorrect.name"));
        }
        switch (type) {
            case "file" -> copyFile(currentName, newName);
            case "folder" -> copyFolder(currentName, newName);
        }
        delete(currentName, type);
    }


    public List<MinioFileDto> getAllFilesInFolder(String folderName) {
        return getAllObjectInFolder(folderName, false)
                .stream()
                .filter(item -> !item.isDir())
                .filter(item -> !item.objectName().equals(folderName + "/"))
                .map(minioMapper::itemToMinioFileDto)
                .toList();
    }

    public void uploadFile(MultipartFile file, String path) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new MinioUploadException(getErrorMessage("upload") + e.getMessage());
        }
    }

    public GetObjectResponse downloadFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            throw new MinioDownloadException(getErrorMessage("download") + e.getMessage());
        }
    }

    public List<MinioFileDto> searchFileByName(String folderName, String fileName) {
        return getAllObjectInFolder(folderName, true).stream()
                .filter(item -> !item.objectName().endsWith("/"))
                .map(minioMapper::itemToMinioFileDto)
                .filter(file -> file.getName().startsWith(fileName))
                .collect(Collectors.toList());
    }

public boolean isFileExist(String filePath) {
    try {
        return minioClient.statObject(StatObjectArgs.builder().bucket(minioProperties.getBucket())
                .object(filePath)
                .build()) != null;
    } catch (Exception e) {
        return false;
    }
}

    public List<MinioFolderDto> getAllFoldersInFolder(String folderName) {
        return getAllObjectInFolder(folderName, false)
                .stream()
                .filter(item -> item.isDir())
                .map(item -> minioMapper.stringToMinioFolderDto(item.objectName()))
                .toList();
    }

    public Set<String> getAllUsersFolders(String parentFolderName) {
        return getAllObjectInFolder(parentFolderName, true).stream()
                .map(item -> item.objectName())
                .filter(path -> path.indexOf("/") != path.lastIndexOf("/"))
                .map(path -> path.substring(0, path.lastIndexOf("/") + 1))
                .collect(Collectors.toSet());
    }

    public void createUserFolder(Long id) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(FolderUtil.getUserFolderName(id) + "/")
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioCreateException(getErrorMessage("createFolder") + e.getMessage());
        }
    }

    public void createEmptyFolderWithName(String parentFolder, String folderName) {
        if (!isNameCorrect(folderName)){
            throw new IncorrectNameException(getErrorMessage("incorrect.name"));
        }
        if (isFolderExist(parentFolder + "/" + folderName)) {
            throw new MinioDuplicateNameException(getErrorMessage("duplicate"));
        }

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(parentFolder + "/" + folderName + "/")
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioUploadException(getErrorMessage("upload") + e.getMessage());
        }
    }


    public ByteArrayOutputStream downloadFolder(String folderName) {
        try {
            var baos = new ByteArrayOutputStream();
            try (var outputStream = new ZipOutputStream(baos)) {
                var allObjectsInFolder = getAllObjectInFolder(folderName, true);
                for (Item result : allObjectsInFolder) {
                    var object = minioClient.getObject(GetObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(result.objectName())
                            .build());
                    outputStream.putNextEntry(new ZipEntry(result.objectName()));
                    var buffer = new byte[1024];
                    int length;
                    while ((length = object.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.closeEntry();
                }
            }
            return baos;
        } catch (Exception e) {
            throw new MinioDownloadException(getErrorMessage("download") + e.getMessage());
        }
    }

    public List<MinioFolderDto> searchFolderByName(String parentFolderName, String folderName) {
        return getAllUsersFolders(parentFolderName).stream()
                .map(minioMapper::stringToMinioFolderDto)
                .filter(folder -> folder.getName().startsWith(folderName))
                .collect(Collectors.toList());
    }

    public Boolean isFolderExist(String folderPath) {
        return !getAllObjectInFolder(folderPath, true).isEmpty();
    }

    public Long getUsedMemory(String userFolder) {
        return getAllObjectInFolder(userFolder, true).stream()
                .mapToLong(it -> it.size())
                .sum();
    }

    public boolean isFileFitOnDisk(String userFolder, Long fileSize) {
        return MAX_MEMORY - getUsedMemory(userFolder) > fileSize;
    }
    public void checkFileFitOnDisk(String userFolder, long totalSize) {
        if (!isFileFitOnDisk(userFolder, totalSize)) {
            throw new MemoryLlimitException(getErrorMessage("memory.limit"));
        }
    }

    public String getProcessText(String userFolder){
             return messageSource.getMessage(
                "process.text",
                new String[]{FileUtil.getFileSize(getUsedMemory(userFolder)), FileUtil.getFileSize(MAX_MEMORY)},
                LocaleContextHolder.getLocale());
    }


    public Long getObjectSize(String path, String type) {
        try {
            if (type.equals("file")) {
                return minioClient.statObject(StatObjectArgs.builder()
                                .bucket(minioProperties.getBucket())
                                .object(path).build())
                        .size();
            }
            return getAllObjectInFolder(path, true)
                    .stream()
                    .mapToLong(item -> item.size())
                    .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
    public Long getPercent(String userFolder){
       return getUsedMemory(userFolder) * 100 / MAX_MEMORY;
    }


    private void copyFile(String currentName, String newName) {
        var newFilePath = FolderUtil.getParentFolderPath(currentName) + "/" + newName + FileUtil.getFileType(currentName);
        if (FileUtil.getFileName(currentName).equals(newName) || isFileExist(newFilePath)) {
            throw new MinioDuplicateNameException(getErrorMessage("duplicate"));
        }
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .source(CopySource.builder()
                            .bucket(minioProperties.getBucket())
                            .object(currentName)
                            .build())
                    .bucket(minioProperties.getBucket())
                    .object(newFilePath)
                    .build());
        } catch (Exception e) {
            throw new MinioCopyException(getErrorMessage("copy") + e.getMessage());
        }
    }

    private void removeObject(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            throw new MinioRemoveException(getErrorMessage("remove") + e.getMessage());
        }
    }

    private boolean isNameCorrect(String name) {
        return name.matches("^[\\p{L}0-9_-]{1,30}$");
    }

    private void copyFolder(String folderPath, String newName) {
        var newPathFolder = FolderUtil.getNewPathFolder(folderPath, newName);
        if (folderPath.equals(newPathFolder) || isFolderExist(newPathFolder)) {
            throw new MinioDuplicateNameException(getErrorMessage("duplicate"));
        }
        try {
            for (Item object : getAllObjectInFolder(folderPath, true)) {
                minioClient.copyObject(CopyObjectArgs.builder()
                        .source(CopySource.builder()
                                .bucket(minioProperties.getBucket())
                                .object(object.objectName())
                                .build())
                        .bucket(minioProperties.getBucket())
                        .object(object.objectName().replace(folderPath, newPathFolder))
                        .build());
            }
        } catch (Exception e) {
            throw new MinioCopyException(getErrorMessage("copy") + e.getMessage());
        }
    }

    private List<Item> getAllObjectInFolder(String folderName, Boolean recursive) {
        var results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(minioProperties.getBucket())
                .prefix(folderName + "/")
                .recursive(recursive)
                .build());
        var objects = new ArrayList<Item>();
        for (Result<Item> result : results) {
            try {
                objects.add(result.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return objects;
    }

    private String getErrorMessage(String errorType) {
        return messageSource.getMessage("error.message." + errorType, null, LocaleContextHolder.getLocale());
    }

}
