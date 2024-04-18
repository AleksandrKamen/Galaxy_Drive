package com.galaxy.galaxy_drive.controller;

import com.galaxy.galaxy_drive.exception.minio.FolderNotFoundException;
import com.galaxy.galaxy_drive.model.dto.user.UserReadDto;
import com.galaxy.galaxy_drive.service.FileStorageService;
import com.galaxy.galaxy_drive.service.user.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import static com.galaxy.galaxy_drive.util.AuthenticationUtil.*;
import static com.galaxy.galaxy_drive.util.FileStorageUtil.*;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@SessionAttributes({"user", "userFolder"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IndexController {
    UserService userService;
    FileStorageService fileStorageService;

    @GetMapping
    public String homePage(@RequestParam(required = false) String path,
                           @AuthenticationPrincipal Object principal,
                           @SessionAttribute(value = "user", required = false) UserReadDto user,
                           Model model) {
        if (user == null) {
            user = userService.findByUserName(getUserName(principal));
        }
        var userFolder = getUserFolderName(user.getId());

        if (path == null || path.isEmpty()) {
            return "redirect:/?path=" + userFolder;
        }

        if (!path.startsWith(userFolder) || !fileStorageService.isFolderExist(path)) {
            throw new FolderNotFoundException("Folder not found or not exist");
        }

        model.addAllAttributes(Map.of(
                "user", user,
                "allFoldersInFolder", fileStorageService.getAllFoldersInFolder(path),
                "allFilesInFolder", fileStorageService.getAllFilesInFolder(path),
                "parentFolders", getBreadcrumbs(path),
                "userFolder", userFolder,
                "path", path,
                "memoryText", fileStorageService.getMemoryUsedText(userFolder),
                "percentUsedMemory", fileStorageService.getPercentUsedMemory(userFolder)
        ));
        return "index";
    }

    @PostMapping("update")
    public String update(UserReadDto userReadDto,
                         @AuthenticationPrincipal Object principal,
                         Model model) {
        userService.updateUser(userReadDto);
        model.addAttribute("user", userService.findByUserName(getUserName(principal)));
        return "redirect:/";
    }


}
