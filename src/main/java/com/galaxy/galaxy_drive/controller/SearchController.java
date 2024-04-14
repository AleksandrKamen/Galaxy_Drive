package com.galaxy.galaxy_drive.controller;

import com.galaxy.galaxy_drive.model.dto.user.UserReadDto;
import com.galaxy.galaxy_drive.service.minio.MinioService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
@SessionAttributes("user")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchController {
    MinioService minioService;

    @GetMapping
    public String searchPage(@RequestParam String query,
                             @SessionAttribute(value = "user") UserReadDto user,
                             @SessionAttribute(value = "userFolder") String userFolder,
                             Model model) {
        model.addAllAttributes(
                Map.of(
                        "user", user,
                        "foundFolders", minioService.searchFolderByName(userFolder, query),
                        "foundFiles", minioService.searchFileByName(userFolder, query),
                        "query", query,
                        "userFolder", userFolder)
        );
        return "search";
    }
}
