package com.galaxy.galaxy_drive.service.integration;

import com.galaxy.galaxy_drive.model.repository.MinioRepository;
import com.galaxy.galaxy_drive.service.minio.MinioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

@Testcontainers
@SpringBootTest
public class MinioServiceIntegrationIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres");
    @Container
    static MinIOContainer minIOContainer = new MinIOContainer("minio/minio")
            .withUserName("user")
            .withPassword("password");


    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", minIOContainer::getS3URL);
        registry.add("minio.access-key", () -> "user");
        registry.add("minio.secret-key", () -> "password");
    }

    @Autowired
    MinioService minioService;
    @Autowired
    MinioRepository minioRepository;

    static final String PARENT_FOLDER = "user-1-files";
    static final String FOLDER_PATH = "user-1-files/testFolder";
    static final String REFERER_TEST = "http://localhost:8080/?path=user-1-files/testFolder";

    @BeforeEach
    void createFolder() {
        minioService.createUserFolder(1L);
    }

    @AfterEach
    void removeFolder() {
        minioRepository.getAllObjectInFolder(FOLDER_PATH, true).stream()
                .forEach(it -> minioRepository.removeObject(it.objectName()));
    }

    @Test
    void createUserFolder() {
        assertTrue(minioService.isFolderExist(PARENT_FOLDER));
    }

    @Test
    void uploadFile() {
        var file = getMockFile("test.txt");
        minioService.uploadFiles(List.of(file), REFERER_TEST, PARENT_FOLDER);

        var actualResult = minioService.getAllFilesInFolder(FOLDER_PATH);
        assertThat(actualResult).hasSize(1);
    }

    @Test
    void deleteFile() {
        var file = getMockFile("test.txt");
        minioService.uploadFiles(List.of(file), REFERER_TEST, PARENT_FOLDER);
        assertThat(minioService.getAllFilesInFolder(FOLDER_PATH)).hasSize(1);
        minioRepository.removeObject(FOLDER_PATH + "/" + file.getOriginalFilename());
        var actualResult = minioService.getAllFilesInFolder(FOLDER_PATH);
        assertThat(actualResult).isEmpty();
    }

    @Test
    void deleteFolderWithFiles() {
        minioService.createUserFolder(2L);
        var file = getMockFile("test.txt");
        minioService.uploadFiles(List.of(file), "user-2-files" + "/" + file.getOriginalFilename(), PARENT_FOLDER);
        minioRepository.getAllObjectInFolder("user-2-files", true).stream()
                .forEach(it -> minioRepository.removeObject(it.objectName()));
        assertFalse(minioService.isFolderExist("user-2-files"));
    }

    @Test
    void renameFile() {
        var file = getMockFile("test.txt");
        minioService.uploadFiles(List.of(file), REFERER_TEST, PARENT_FOLDER);
        minioService.copyFile(FOLDER_PATH + "/" + file.getOriginalFilename(), "test2");
        minioRepository.removeObject(FOLDER_PATH + "/" + file.getOriginalFilename());
        var actualResult = minioService.getAllFilesInFolder(FOLDER_PATH);
        assertThat(actualResult).hasSize(1);
        assertThat(actualResult.get(0).getName()).isEqualTo("test2.txt");
    }

    @Test
    void renameFolderWithFiles() {
        var file = getMockFile("test.txt");
        minioService.uploadFiles(List.of(file), REFERER_TEST, PARENT_FOLDER);
        minioService.copyFolder(FOLDER_PATH, "newFolderName");
        minioRepository.getAllObjectInFolder(FOLDER_PATH, true).stream()
                .forEach(it -> minioRepository.removeObject(it.objectName()));
        assertTrue(minioService.isFolderExist(PARENT_FOLDER + "/" + "newFolderName"));
        assertThat(minioService.getAllFilesInFolder(PARENT_FOLDER + "/" + "newFolderName")).hasSize(1);
        assertFalse(minioService.isFolderExist(FOLDER_PATH));
    }

    @Test
    void createEmptyFolder() {
        minioService.createEmptyFolder(FOLDER_PATH, "EmptyFolder");
        assertTrue(minioService.isFolderExist(FOLDER_PATH + "/" + "EmptyFolder"));
    }

    @Test
    void copyFile() {
        var file = getMockFile("test.txt");
        minioService.uploadFiles(List.of(file), REFERER_TEST, PARENT_FOLDER);
        minioService.copyFile(FOLDER_PATH + "/" + file.getOriginalFilename(), "copyName");
        var actualResult = minioService.getAllFilesInFolder(FOLDER_PATH);
        actualResult.stream().filter(item -> item.getName().equals("copyName.txt"));
        assertThat(actualResult).hasSize(2);
        assertNotNull(actualResult.stream().filter(it -> it.getName().equals("copyName.txt")).findFirst());
    }

    @Test
    void copyFolder() {
        minioService.copyFolder(FOLDER_PATH, "newFolder");
        minioService.isFolderExist(FOLDER_PATH);
        minioService.isFolderExist("newFolder");
    }

    @Test
    void searchFoldersByName() {
        minioService.createEmptyFolder(FOLDER_PATH, "TestFolder");
        minioService.createEmptyFolder(FOLDER_PATH, "TestFolder2");
        assertThat(minioService.searchFolderByName(FOLDER_PATH, "test")).isEmpty();
        assertThat(minioService.searchFolderByName(FOLDER_PATH, "Test")).hasSize(2);
    }

    @Test
    void searchFileByName() {
        var file = getMockFile("SearchTest.txt");
        minioService.uploadFiles(List.of(file), FOLDER_PATH + "/" + file.getOriginalFilename(), PARENT_FOLDER);
        var actualResult = minioService.searchFileByName(PARENT_FOLDER, "Search");
        assertThat(actualResult).hasSize(1);
    }


    private static MockMultipartFile getMockFile(String fileName) {
        return new MockMultipartFile("name",
                fileName,
                "text/plain",
                "Test Content".getBytes());
    }


}
