package com.galaxy.galaxy_drive.model.repository;

import com.galaxy.galaxy_drive.exception.minio.*;
import com.galaxy.galaxy_drive.props.MinioProperties;
import com.galaxy.galaxy_drive.service.MessageService;
import io.minio.*;
import io.minio.messages.Item;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import static com.galaxy.galaxy_drive.util.FileStorageUtil.*;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinioRepository {
    MinioClient minioClient;
    MinioProperties minioProperties;
    MessageService messageService;
    static String COUNT_COPY_FORMAT = "(%d)";

    public void putObject(String objectName, InputStream inputStream, Long objectSize) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .stream(inputStream, objectSize, -1)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new MinioUploadException(messageService.getErrorMessage("upload") + e.getMessage());
        }
    }

    public Long getObjectSize(String objectPath) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectPath).build())
                    .size();
        } catch (Exception e) {
            throw new MinioObjectSizeException(messageService.getErrorMessage("objectSize") + e.getMessage());
        }

    }

    public boolean isObjectExist(String filePath) {
        try {
            return minioClient.statObject(StatObjectArgs.builder().bucket(minioProperties.getBucket())
                    .object(filePath)
                    .build()) != null;
        } catch (Exception e) {
            return false;
        }

    }

    public GetObjectResponse downloadObject(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            throw new MinioDownloadException(messageService.getErrorMessage("download") + e.getMessage());
        }
    }

    public void copyObject(String from, String to) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .source(CopySource.builder()
                            .bucket(minioProperties.getBucket())
                            .object(from)
                            .build())
                    .bucket(minioProperties.getBucket())
                    .object(to)
                    .build());
        } catch (Exception e) {
            throw new MinioCopyException(messageService.getErrorMessage("copy") + e.getMessage());
        }
    }

    public void removeObject(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            throw new MinioRemoveException(messageService.getErrorMessage("remove") + e.getMessage());
        }
    }

    public List<Item> getAllObjectInFolder(String folderName, Boolean recursive) {
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

    public void uploadFile(MultipartFile file, String path) {
        try {
            putObject(path, file.getInputStream(), file.getSize());
        } catch (IOException e) {
            throw new MinioUploadException(messageService.getErrorMessage("upload") + e.getMessage());
        }
    }

    public Long getObjectSizeByType(String path, String type) {
        return type.equals("file") ? getObjectSize(path) :
                getAllObjectInFolder(path, true).stream()
                        .mapToLong(item -> item.size())
                        .sum();
    }

    public String generateCopyFilePath(String referer, MultipartFile file) {
        var count = 1;
        var filePath = getPathParam(referer) + "/" + file.getOriginalFilename();
        var newFilePath = filePath;
        while (isObjectExist(newFilePath)) {
            newFilePath = filePath.substring(0, filePath.indexOf('.')) + String.format(COUNT_COPY_FORMAT, count++) + getFileType(file.getOriginalFilename());
        }
        return newFilePath;
    }

    public Boolean isFileFitOnDisk(String userFolder, long filesSize) {
        return minioProperties.getMemoryLimit() - getObjectSizeByType(userFolder, "folder") > filesSize;
    }

    public String getMemoryUsedText(String userFolder) {
        return messageService.getMessage("process.text", new String[]{getFileSize(getObjectSizeByType(userFolder, "folder")),
                getFileSize(minioProperties.getMemoryLimit())});
    }

    public Long getPercentUsedMemory(String userFolder) {
        return getObjectSizeByType(userFolder, "folder") * 100 / minioProperties.getMemoryLimit();
    }

}
