package com.galaxy.galaxy_drive.service.integration;

import com.galaxy.galaxy_drive.service.minio.MinioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class MinioServiceIntegrationIT extends IntegrationTestBase {
    @Autowired
    MinioService minioService;
    static final String FOLDER_TYPE = "folder";
    static final String FILE_TYPE = "file";
    static final String PARENT_FOLDER = "user-1-files";
    static final String FOLDER_PATH = "user-1-files/testFolder";

    @BeforeEach
    void createFolder() {
        minioService.createUserFolder(1L);
    }

    @AfterEach
    void removeFolder() {
        minioService.delete(FOLDER_PATH, FOLDER_TYPE);
    }

    @Test
    void createUserFolder() {
        assertTrue(minioService.isFolderExist(PARENT_FOLDER));
    }

    @Test
    void uploadFile() {
        var file = getMockFile("test.txt");
        minioService.uploadFile(file, FOLDER_PATH + "/" + file.getOriginalFilename());
        var actualResult = minioService.getAllFilesInFolder(FOLDER_PATH);
        assertThat(actualResult).hasSize(1);
    }

    @Test
    void deleteFile() {
        var file = getMockFile("test.txt");
        minioService.uploadFile(file, FOLDER_PATH + "/" + file.getOriginalFilename());
        assertThat(minioService.getAllFilesInFolder(FOLDER_PATH)).hasSize(1);
        minioService.delete(FOLDER_PATH + "/" + file.getOriginalFilename(), FILE_TYPE);
        var actualResult = minioService.getAllFilesInFolder(FOLDER_PATH);
        assertThat(actualResult).isEmpty();
    }

    @Test
    void deleteFolderWithFiles() {
        minioService.createUserFolder(2L);
        var file = getMockFile("test.txt");
        minioService.uploadFile(file, "user-2-files" + "/" + file.getOriginalFilename());
        minioService.delete("user-2-files", FOLDER_TYPE);
        assertFalse(minioService.isFolderExist("user-2-files"));
    }

    @Test
    void renameFile() {
        var file = getMockFile("test.txt");
        minioService.uploadFile(file, FOLDER_PATH + "/" + file.getOriginalFilename());
        minioService.rename(FOLDER_PATH + "/" + file.getOriginalFilename(), "test2", FILE_TYPE);
        var actualResult = minioService.getAllFilesInFolder(FOLDER_PATH);
        assertThat(actualResult).hasSize(1);
        assertThat(actualResult.get(0).getName()).isEqualTo("test2.txt");
    }

    @Test
    void renameFolderWithFiles() {
        var file = getMockFile("test.txt");
        minioService.uploadFile(file, FOLDER_PATH + "/" + file.getOriginalFilename());
        minioService.rename(FOLDER_PATH, "newFolderName", FOLDER_TYPE);
        assertTrue(minioService.isFolderExist(PARENT_FOLDER + "/" + "newFolderName"));
        assertThat(minioService.getAllFilesInFolder(PARENT_FOLDER + "/" + "newFolderName")).hasSize(1);
        assertFalse(minioService.isFolderExist(FOLDER_PATH));
    }

    @Test
    void createEmptyFolder() {
        minioService.createEmptyFolderWithName(FOLDER_PATH, "EmptyFolder");
        assertTrue(minioService.isFolderExist(FOLDER_PATH + "/" + "EmptyFolder"));
    }

    @Test
    void copyFile() {
        var file = getMockFile("test.txt");
        minioService.uploadFile(file, FOLDER_PATH + "/" + file.getOriginalFilename());
        minioService.copy(FOLDER_PATH + "/" + file.getOriginalFilename(), "copyName", FILE_TYPE);
        var actualResult = minioService.getAllFilesInFolder(FOLDER_PATH);
        actualResult.stream().filter(item -> item.getName().equals("copyName.txt"));
        assertThat(actualResult).hasSize(2);
        assertNotNull(actualResult.stream().filter(it -> it.getName().equals("copyName.txt")).findFirst());
    }

    @Test
    void copyFolder() {
        minioService.copy(FOLDER_PATH, "newFolder", FOLDER_TYPE);
        minioService.isFolderExist(FOLDER_PATH);
        minioService.isFolderExist("newFolder");
    }

    @Test
    void searchFoldersByName() {
        minioService.createEmptyFolderWithName(FOLDER_PATH, "TestFolder");
        minioService.createEmptyFolderWithName(FOLDER_PATH, "TestFolder2");
        assertThat(minioService.searchFolderByName(FOLDER_PATH, "test")).isEmpty();
        assertThat(minioService.searchFolderByName(FOLDER_PATH, "Test")).hasSize(2);
    }

    @Test
    void searchFileByName() {
        var file = getMockFile("SearchTest.txt");
        minioService.uploadFile(file, FOLDER_PATH + "/" + file.getOriginalFilename());
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
