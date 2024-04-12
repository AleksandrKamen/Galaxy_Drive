package com.galaxy.galaxy_drive.controller;

import com.galaxy.galaxy_drive.service.minio.MinioService;
import com.galaxy.galaxy_drive.service.user.UserService;
import com.galaxy.galaxy_drive.util.AuthenticationUtil;
import com.galaxy.galaxy_drive.util.FolderUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchController {
     MinioService minioService;
     UserService userService;
    @GetMapping
    public String searchPage(@RequestParam String query,
                             @AuthenticationPrincipal Object principal,
                             Model model){
        var user = userService.findByUserName(AuthenticationUtil.getUserName(principal));
        var userFolderPath = FolderUtil.getUserFolderName(user.getId());

        model.addAllAttributes(
                Map.of(
                        "user", user,
                        "foundFolders", minioService.searchFolderByName(userFolderPath, query),
                        "foundFiles", minioService.searchFileByName(userFolderPath, query),
                        "query", query,
                        "userFolderName", userFolderPath)
                );
        return "search";
    }
}
