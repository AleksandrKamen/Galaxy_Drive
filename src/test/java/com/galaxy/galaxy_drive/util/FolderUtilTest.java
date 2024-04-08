package com.galaxy.galaxy_drive.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FolderUtilTest {

    static final String FOLDER_PATH_TEST = "user-1-files/parentFolder/testFolder";


    @Test
    void getParentFolder(){
        var actualResult = FolderUtil.getParentFolderPath(FOLDER_PATH_TEST);
        assertThat(actualResult).isEqualTo("user-1-files/parentFolder");
    }

    @Test
    void getUserFolderName(){
        var actualResult = FolderUtil.getUserFolderName(10L);
        assertThat(actualResult).isEqualTo("user-10-files");
    }

    @Test
    void getNameFolder(){
        var actualResult =  FolderUtil.getNameFolder(FOLDER_PATH_TEST);
        assertThat(actualResult).isEqualTo("testFolder");
    }

    @Test
    void getParentFolders(){
        var actualResult = FolderUtil.getBreadcrumbs(FOLDER_PATH_TEST);
        assertThat(actualResult).hasSize(3);
        assertThat(actualResult).contains("user-1-files", "user-1-files/parentFolder", "user-1-files/parentFolder/testFolder");
    }
    @Test
    void getNewPathFolder(){
        var actualResult = FolderUtil.getNewPathFolder(FOLDER_PATH_TEST, "newFolderName");
        assertThat(actualResult).isEqualTo("user-1-files/parentFolder/newFolderName");
    }
}