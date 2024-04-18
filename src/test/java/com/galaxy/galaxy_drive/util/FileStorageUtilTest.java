package com.galaxy.galaxy_drive.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.galaxy.galaxy_drive.util.FileStorageUtil.getFileSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

class FileStorageUtilTest {

    static final String FOLDER_PATH_TEST = "user-1-files/parentFolder/testFolder";
    static final String FILE_PATH_TEST = "user-1-files/test/test.txt";


    @Test
    void getParentFolder() {
        var actualResult = FileStorageUtil.getParentFolderPath(FOLDER_PATH_TEST);
        assertThat(actualResult).isEqualTo("user-1-files/parentFolder");
    }

    @Test
    void getUserFolderName() {
        var actualResult = FileStorageUtil.getUserFolderName(10L);
        assertThat(actualResult).isEqualTo("user-10-files");
    }

    @Test
    void getNameFolder() {
        var actualResult = FileStorageUtil.getNameFolder(FOLDER_PATH_TEST);
        var actualResult2 = FileStorageUtil.getNameFolder(FOLDER_PATH_TEST + "/");
        assertThat(actualResult).isEqualTo("testFolder");
        assertThat(actualResult).isEqualTo("testFolder");
    }

    @Test
    void getParentFolders() {
        var actualResult = FileStorageUtil.getBreadcrumbs(FOLDER_PATH_TEST);
        assertThat(actualResult).hasSize(3);
        assertThat(actualResult).contains("user-1-files", "user-1-files/parentFolder", "user-1-files/parentFolder/testFolder");
    }

    @Test
    void getNewPathFolder() {
        var actualResult = FileStorageUtil.getNewPathFolder(FOLDER_PATH_TEST, "newFolderName");
        assertThat(actualResult).isEqualTo("user-1-files/parentFolder/newFolderName");
    }

    @Test
    void getFileType() {
        var actualResult = FileStorageUtil.getFileType(FILE_PATH_TEST);
        var actualResult2 = FileStorageUtil.getFileType("java.1.txt");
        assertThat(actualResult).isEqualTo(".txt");
        assertThat(actualResult2).isEqualTo(".txt");
    }

    @Test
    void getFileName() {
        var actualResult = FileStorageUtil.getFileName(FILE_PATH_TEST, false);
        var actualResult2 = FileStorageUtil.getFileName(FILE_PATH_TEST, true);
        assertThat(actualResult).isEqualTo("test");
        assertThat(actualResult2).isEqualTo("test.txt");
    }

    @Test
    void getSize() {
        var actualResult = getFileSize(10240L);
        var actualResult2 = getFileSize(10485760L);
        var actualResult3 = getFileSize(10737418240L);
        assertThat(actualResult).isEqualTo("10 KB");
        assertThat(actualResult2).isEqualTo("10 MB");
        assertThat(actualResult3).isEqualTo("10 GB");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Test", "Java.Последнее Издание-2", "Тест", "Test Test", "Тест Тест - 22", "........", "(123)-123-.", "1    "})
    void NameCorrect(String fileName){
        var actualResult = FileStorageUtil.isNameCorrect(fileName);
        assertTrue(actualResult);

    }
    @ParameterizedTest
    @ValueSource(strings = {"Test/Test", "!!!Test", " ", "TestTestTestTestTestTestTestTest"})
    void NameNotCorrect(String fileName){
        var actualResult = FileStorageUtil.isNameCorrect(fileName);
        assertFalse(actualResult);

    }


}