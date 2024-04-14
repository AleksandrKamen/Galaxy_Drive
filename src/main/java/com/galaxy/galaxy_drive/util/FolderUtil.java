package com.galaxy.galaxy_drive.util;

import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.util.UriEncoder;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class FolderUtil {
   private Integer PARENT_FOLDERS_COUNT = 3;

    public String getUserFolderName(Long id) {
        return "user-" + id + "-files";
    }

    public List<String> getBreadcrumbs(String folderPath) {
        var breadcrumbs = new ArrayList<String>();
        breadcrumbs.add(folderPath);
        while (folderPath.contains("/")) {
            var parentFolderPath = FolderUtil.getParentFolderPath(folderPath);
            breadcrumbs.add(0, parentFolderPath);
            folderPath = parentFolderPath;
        }
        return breadcrumbs.stream()
                .skip(breadcrumbs.size() > PARENT_FOLDERS_COUNT ? breadcrumbs.size() - PARENT_FOLDERS_COUNT : 0)
                .toList();
    }
    public String getParentFolderPath(String path){
        if (path.endsWith("/")){
            var substring = path.substring(0, path.length() - 1);
            return substring.substring(0, substring.lastIndexOf("/"));
        }
        return path.substring(0, path.lastIndexOf("/"));
    }

    public String getNameFolder(String folderPath){
        if (folderPath.endsWith("/")){
            var substring = folderPath.substring(0, folderPath.length() - 1);
            return substring.substring(substring.lastIndexOf("/")+1);
        }
        return  folderPath.substring(folderPath.lastIndexOf("/") + 1);
    }

    public String getNewPathFolder(String currentName, String newName){
        return getParentFolderPath(currentName) + "/" + newName;
    }



}
