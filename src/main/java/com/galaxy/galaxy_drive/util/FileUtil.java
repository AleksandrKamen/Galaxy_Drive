package com.galaxy.galaxy_drive.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtil {
    private final Long GB_SIZE = 1073741824L;
    private final Long MB_SIZE = 1048576L;
    private final Long KB_SIZE = 1024L;
    public String getFileDir(String filePath) {
        var lastIndexOf = filePath.lastIndexOf("/");
        return filePath.substring(0, lastIndexOf + 1);
    }
    public String getFileType(String filePath) {
        var indexOf = filePath.indexOf(".");
        return filePath.substring(indexOf, filePath.length());
    }

    public String getFileName(String filePath) {
        var indexOf = filePath.indexOf(".");
        return filePath.substring(filePath.lastIndexOf("/") + 1, indexOf);
    }
    public String getFileNameWithType(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }
    public String getParentFolderPath (String filePath) {
        return filePath.substring(0, filePath.lastIndexOf("/"));
    }

    public String getSize(Long size){
        if (size > GB_SIZE){
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
