package com.galaxy.galaxy_drive.model.repository;

import com.galaxy.galaxy_drive.props.MinioProperties;
import io.minio.*;
import io.minio.messages.Item;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinioRepository {
    MinioClient minioClient;
    MinioProperties minioProperties;

    @SneakyThrows
    public void putObject(String objectName, InputStream inputStream, Long objectSize) {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioProperties.getBucket())
                .stream(inputStream, objectSize, -1)
                .object(objectName)
                .build());
    }

    @SneakyThrows
    public Long getObjectSize(String objectPath) {
        return minioClient.statObject(StatObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(objectPath).build())
                .size();
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

    @SneakyThrows
    public GetObjectResponse downloadObject(String fileName) {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(fileName)
                        .build());
    }

    @SneakyThrows
    public void copyObject(String from, String to) {
        minioClient.copyObject(CopyObjectArgs.builder()
                .source(CopySource.builder()
                        .bucket(minioProperties.getBucket())
                        .object(from)
                        .build())
                .bucket(minioProperties.getBucket())
                .object(to)
                .build());
    }

    @SneakyThrows
    public void removeObject(String fileName) {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(minioProperties.getBucket())
                .object(fileName)
                .build());
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

}
