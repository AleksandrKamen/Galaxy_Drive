package com.galaxy.galaxy_drive.initializer;

import com.galaxy.galaxy_drive.props.MinioProperties;
import com.galaxy.galaxy_drive.service.minio.BucketService;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinioBucketInitializer {
    BucketService bucketService;
    MinioProperties properties;

    @PostConstruct
    public void initBucketIfNotExist() {
        bucketService.createBucketIfNotExist(properties.getBucket());
    }
}
