package com.galaxy.galaxy_drive.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class FileUtilTest {

    static final String FILE_PATH_TEST = "user-1-files/test/test.txt";

    @Test
    void getFileType() {
        var actualResult = FileUtil.getFileType(FILE_PATH_TEST);
        assertThat(actualResult).isEqualTo(".txt");
    }

    @Test
    void getFileName() {
        var actualResult = FileUtil.getFileName(FILE_PATH_TEST);
        assertThat(actualResult).isEqualTo("test");
    }

    @Test
    void getFileNameWithType() {
        var actualResult = FileUtil.getFileNameWithType(FILE_PATH_TEST);
        assertThat(actualResult).isEqualTo("test.txt");
    }

    @Test
    void getSize() {
        var actualResult = FileUtil.getFileSize(10240L);
        var actualResult2 = FileUtil.getFileSize(10485760L);
        var actualResult3 = FileUtil.getFileSize(10737418240L);
        assertThat(actualResult).isEqualTo("10 KB");
        assertThat(actualResult2).isEqualTo("10 MB");
        assertThat(actualResult3).isEqualTo("10 GB");
    }

}