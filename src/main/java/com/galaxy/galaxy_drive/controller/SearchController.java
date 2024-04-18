package com.galaxy.galaxy_drive.controller;

import com.galaxy.galaxy_drive.service.FileStorageService;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchController {
    FileStorageService fileStorageService;

    @GetMapping
    public String searchPage(@RequestParam String query,
                             @SessionAttribute(value = "userFolder") String userFolder,
                             Model model) {
        model.addAllAttributes(
                Map.of(
                        "foundFolders", fileStorageService.searchFolderByName(userFolder, query),
                        "foundFiles", fileStorageService.searchFileByName(userFolder, query)
                ));
        return "search";
    }
}
