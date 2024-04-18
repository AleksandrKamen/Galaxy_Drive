package com.galaxy.galaxy_drive.util;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileStorageUtil {
    static final Integer PARENT_FOLDERS_COUNT = 3;
    static final Long KB_SIZE = 1024L;
    static final Long MB_SIZE = 1048576L;
    static final Long GB_SIZE = 1073741824L;
    static final String B_DESIGNATION = " B";
    static final String KB_DESIGNATION = " KB";
    static final String MB_DESIGNATION = " MB";
    static final String GB_DESIGNATION = " GB";

    public String getUserFolderName(Long id) {
        return "user-" + id + "-files";
    }

    public List<String> getBreadcrumbs(String folderPath) {
        var breadcrumbs = new ArrayList<String>();
        breadcrumbs.add(folderPath);
        while (folderPath.contains("/")) {
            var parentFolderPath = getParentFolderPath(folderPath);
            breadcrumbs.add(0, parentFolderPath);
            folderPath = parentFolderPath;
        }
        return breadcrumbs.stream()
                .skip(breadcrumbs.size() > PARENT_FOLDERS_COUNT ? breadcrumbs.size() - PARENT_FOLDERS_COUNT : 0)
                .toList();
    }

    public String getParentFolderPath(String path) {
        var substring = path.substring(0, path.length() - 1);
        return path.endsWith("/") ? substring.substring(0, substring.lastIndexOf("/"))
                : path.substring(0, path.lastIndexOf("/"));
    }

    public String getNameFolder(String folderPath) {
        var substring = folderPath.substring(0, folderPath.length() - 1);
        return folderPath.endsWith("/") ? substring.substring(substring.lastIndexOf("/") + 1) :
                folderPath.substring(folderPath.lastIndexOf("/") + 1);
    }

    public String getNewPathFolder(String currentName, String newName) {
        return getParentFolderPath(currentName) + "/" + newName;
    }


    public String getFileType(String filePath) {
        return filePath.substring(filePath.lastIndexOf("."));
    }

    public String getFileName(String filePath, boolean withType) {
        var lastIndexOf = filePath.lastIndexOf("/") + 1;
        return withType ? filePath.substring(lastIndexOf)
                : filePath.substring(lastIndexOf, filePath.lastIndexOf("."));
    }

    public String getFileSize(Long size) {

        if (size >= GB_SIZE) {
            return size / GB_SIZE + GB_DESIGNATION;
        }
        if (size > MB_SIZE) {
            return size / MB_SIZE + MB_DESIGNATION;
        }
        if (size > KB_SIZE) {
            return size / KB_SIZE + KB_DESIGNATION;
        }
        return size + B_DESIGNATION;
    }

    public String encodeString(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    public String getPathParam(String referer) {
        var indexOf = referer.indexOf("=") + 1;
        return referer.contains("&") ? URLDecoder.decode(referer.substring(indexOf, referer.indexOf("&")))
                : URLDecoder.decode(referer.substring(indexOf));
    }

    public boolean isNameCorrect(String name) {
        return !name.isBlank() && name.matches("^[\\p{L}0-9_()\\s.-]{1,30}$");
    }

    public String getNameByType(String path, String type) {
        return type.equals("folder") ? getNameFolder(path) : getFileName(path, true);
    }


}
