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

    public List<String> getParentFolders(String path) {
        var parentFolders = new ArrayList<String>();
        parentFolders.add(path);
        while (path.contains("/")) {
            var parentFolderPath = path.substring(0, path.lastIndexOf("/"));
            parentFolders.add(0, parentFolderPath);
            path = parentFolderPath;
        }
        return parentFolders.stream()
                .skip(parentFolders.size() > PARENT_FOLDERS_COUNT ? parentFolders.size() - PARENT_FOLDERS_COUNT : 0)
                .toList();
    }

    public String getShortNameFolder(String folderName){
        var lastIndexOf = folderName.lastIndexOf("/");
        return  UriEncoder.encode(folderName.substring(lastIndexOf + 1));
    }

    public String getNewPathFolder(String currentName, String newName){
        var lastIndexOf = currentName.lastIndexOf("/");
        var folderDir = currentName.substring(0, lastIndexOf +1);
        return folderDir + newName;
    }


}
