package com.galaxy.galaxy_drive.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtil {
    private final Long KB_SIZE = 1024L;
    private final Long MB_SIZE = 1048576L;
    private final Long GB_SIZE = 1073741824L;

    public String getFileType(String filePath) {
        return filePath.substring(filePath.indexOf("."));
    }

    public String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1, filePath.indexOf("."));
    }
    public String getFileNameWithType(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    public String getFileSize(Long size){

        if (size >= GB_SIZE){
            return size/GB_SIZE + " GB";
        }
        if (size > MB_SIZE){
            return size/MB_SIZE + " MB";
        }
        if (size > KB_SIZE){
            return size/KB_SIZE + " KB";
        }
        return size + " B";
    }

}
