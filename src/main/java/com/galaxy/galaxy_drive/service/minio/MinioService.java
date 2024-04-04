package com.galaxy.galaxy_drive.service.minio;

import com.galaxy.galaxy_drive.exception.minio.*;
import com.galaxy.galaxy_drive.model.dto.minio.MinioFileDto;
import com.galaxy.galaxy_drive.model.dto.minio.MinioFolderDto;
import com.galaxy.galaxy_drive.model.mapper.minio.MinioFileMapper;
import com.galaxy.galaxy_drive.model.mapper.minio.MinioFolderMapper;
import com.galaxy.galaxy_drive.props.MinioProperties;
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
    MinioFileMapper minioFileMapper;
    MinioFolderMapper minioFolderMapper;
    MessageSource messageSource;

    public void delete(String fileName, String type) {
        switch (type) {
            case "file" -> removeObject(fileName);
            case "folder" -> getAllObjectInFolder(fileName, true).stream()
                    .forEach(it -> removeObject(it.objectName()));
        }
    }

    public void copy(String currentName, String copyName, String type) {
        switch (type) {
            case "file" -> copyFile(currentName, copyName);
            case "folder" -> copyFolder(currentName, copyName);
        }
    }

    public void rename(String currentName, String newName, String type) {
        switch (type) {
            case "file" -> copyFile(currentName, newName);
            case "folder" -> copyFolder(currentName, newName);
        }
        delete(currentName, type);
    }


    //    ********* Files *******************
    public List<MinioFileDto> getAllFilesInFolder(String folderName) {
        return getAllObjectInFolder(folderName, false)
                .stream()
                .filter(item -> !item.isDir())
                .filter(item -> !item.objectName().equals(folderName + "/"))
                .map(minioFileMapper::map)
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
            throw new MinioUploadException(getErrorMessage("upload"));
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
            throw new MinioDownloadException(getErrorMessage("download"));
        }
    }

    public List<MinioFileDto> searchFileByName(String folderName, String fileName) {
        return getAllObjectInFolder(folderName, true).stream()
                .map(minioFileMapper::map)
                .filter(file -> file.getName().startsWith(fileName))
                .collect(Collectors.toList());
    }


    //    ********* Folder *************
    public List<MinioFolderDto> getAllFoldersInFolder(String folderName) {
        return getAllObjectInFolder(folderName, false)
                .stream()
                .filter(item -> item.isDir())
                .map(item -> minioFolderMapper.map(item.objectName()))
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
            throw new MinioCreateException(getErrorMessage("createFolder"));
        }
    }

    public void createEmptyFolderWithName(String parentFolder, String folderName) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(parentFolder + "/" + folderName + "/")
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioUploadException(getErrorMessage("upload"));
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
            throw new MinioDownloadException(getErrorMessage("download"));
        }
    }

    public List<MinioFolderDto> searchFolderByName(String parentFolderName, String folderName) {
        return getAllUsersFolders(parentFolderName).stream()
                .map(minioFolderMapper::map)
                .filter(folder -> folder.getName().startsWith(folderName))
                .collect(Collectors.toList());
    }

    public Boolean isFolderExist(String folderPath) {
        return !getAllObjectInFolder(folderPath, true).isEmpty();
    }

    private void copyFile(String currentName, String newName) {
        var lastIndexOf = currentName.lastIndexOf("/");
        var fileDir = currentName.substring(0, lastIndexOf + 1);
        var indexOf = currentName.indexOf(".");
        var type = currentName.substring(indexOf, currentName.length());

        if (!currentName.substring(lastIndexOf + 1, indexOf).equals(newName)) {
            try {
                minioClient.copyObject(CopyObjectArgs.builder()
                        .source(CopySource.builder()
                                .bucket(minioProperties.getBucket())
                                .object(currentName)
                                .build())
                        .bucket(minioProperties.getBucket())
                        .object(fileDir + newName + type)
                        .build());
            } catch (Exception e) {
                throw new MinioCopyException(getErrorMessage("copy"));
            }

        } else {
            throw new MinioDuplicateNameException(getErrorMessage("duplicate"));
        }
    }

    private void removeObject(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            throw new MinioRemoveException(getErrorMessage("remove"));
        }
    }

    private void copyFolder(String currentName, String newName) {
        var newPathFolder = FolderUtil.getNewPathFolder(currentName, newName);
        if (!currentName.equals(newPathFolder)) {
            try {
                var allObjectInFolder = getAllObjectInFolder(currentName, true);
                for (Item object : allObjectInFolder) {
                    minioClient.copyObject(CopyObjectArgs.builder()
                            .source(CopySource.builder()
                                    .bucket(minioProperties.getBucket())
                                    .object(object.objectName())
                                    .build())
                            .bucket(minioProperties.getBucket())
                            .object(object.objectName().replace(currentName, newPathFolder))
                            .build());
                }
            } catch (Exception e) {
                throw new MinioCopyException(getErrorMessage("copy"));
            }
        } else {
            throw new MinioDuplicateNameException(getErrorMessage("duplicate"));
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
