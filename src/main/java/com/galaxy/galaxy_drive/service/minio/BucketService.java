package com.galaxy.galaxy_drive.service.minio;

import com.galaxy.galaxy_drive.props.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BucketService {
    MinioClient minioClient;
    @SneakyThrows
    public void createBucketIfNotExist(String bucketName) {
        boolean found = isBucketExist(bucketName);
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        }
    }

    @SneakyThrows
    public Boolean isBucketExist(String bucketName) {
        return minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());
    }
}
