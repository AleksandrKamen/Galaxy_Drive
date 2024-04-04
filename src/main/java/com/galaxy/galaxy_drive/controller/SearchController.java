package com.galaxy.galaxy_drive.controller;

import com.galaxy.galaxy_drive.service.minio.MinioService;
import com.galaxy.galaxy_drive.service.user.UserService;
import com.galaxy.galaxy_drive.util.FolderUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
        String userName = null;
        if (principal instanceof UserDetails){
            userName =  ((UserDetails) principal).getUsername();
        } else if(principal instanceof OAuth2User) {
            var email = ((OAuth2User) principal).getAttribute("email");
            var login = ((OAuth2User) principal).getAttribute("login");
            userName = email != null ? email.toString() : login != null ? login.toString() : null;
        }
        var user = userService.findByUserName(userName);
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
