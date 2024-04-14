package com.galaxy.galaxy_drive.controller;

import com.galaxy.galaxy_drive.exception.minio.FolderNotFoundException;
import com.galaxy.galaxy_drive.model.dto.user.UserReadDto;
import com.galaxy.galaxy_drive.service.minio.MinioService;
import com.galaxy.galaxy_drive.service.user.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.yaml.snakeyaml.util.UriEncoder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.galaxy.galaxy_drive.util.AuthenticationUtil.*;
import static com.galaxy.galaxy_drive.util.FileUtil.*;
import static com.galaxy.galaxy_drive.util.FolderUtil.*;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@SessionAttributes({"user", "userFolder"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IndexController {
    UserService userService;
    MinioService minioService;

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

        if (!path.startsWith(userFolder) || !minioService.isFolderExist(path)) {
            throw new FolderNotFoundException("Folder not found or not exist");
        }

        model.addAllAttributes(Map.of(
                "user", user,
                "allFoldersInFolder", minioService.getAllFoldersInFolder(path),
                "allFilesInFolder", minioService.getAllFilesInFolder(path),
                "parentFolders", getBreadcrumbs(path),
                "userFolder", userFolder,
                "path", path,
                "processText", minioService.getProcessText(userFolder),
                "progressValue", minioService.getPercent(userFolder)
        ));
        return "index";
    }

    @PostMapping("update")
    public String update(String userNameInput,
                         String currnetNameInput,
                         String lastNameInput) {
        userService.updateUser(userNameInput, currnetNameInput, lastNameInput);
        return "redirect:/";
    }

    @PostMapping("upload")
    public String uploadFile(@RequestParam("files")
                             List<MultipartFile> files,
                             RedirectAttributes redirectAttributes,
                             @RequestHeader(value = "Referer") String referer,
                             @SessionAttribute(value = "userFolder") String userFolder) {
        minioService.checkFileFitOnDisk(userFolder, files.stream()
                .mapToLong(file -> file.getSize())
                .sum());
        var uploadFiles = files.stream()
                .map(file -> {
                    minioService.uploadFile(file, getFilePath(referer, file));
                    return file.getOriginalFilename();
                })
                .collect(Collectors.toList());
        addRedirectAttributes(redirectAttributes, "upload", uploadFiles);
        return "redirect:" + referer;
    }

    @PostMapping("create")
    public String createFolder(String folderName,
                               RedirectAttributes redirectAttributes,
                               @RequestHeader(value = "Referer") String referer) {
        minioService.createEmptyFolderWithName(getPathParam(referer), folderName);
        addRedirectAttributes(redirectAttributes, "create", List.of(folderName));
        return "redirect:" + referer;
    }

    @PostMapping("delete")
    public String deleteFile(String type,
                             String objectName,
                             RedirectAttributes redirectAttributes,
                             @RequestHeader(value = "Referer") String referer) {
        minioService.delete(objectName, type);
        addRedirectAttributes(redirectAttributes, "delete", List.of(getName(objectName, type)));
        if (!referer.contains("query") && !minioService.isFolderExist(getPathParam(referer))) {
            return "redirect:" + getParentFolderPath(referer);
        }
        return "redirect:" + referer;
    }

    @PostMapping("rename")
    public String renameFile(String type,
                             String currentName,
                             String newName,
                             RedirectAttributes redirectAttributes,
                             @RequestHeader(value = "Referer") String referer) {
        minioService.rename(currentName, newName, type);
        addRedirectAttributes(redirectAttributes, "rename", List.of(getName(currentName, type)));
        return "redirect:" + referer;
    }

    @PostMapping("copy")
    public String copyFile(String type,
                           String currentName,
                           String copyName,
                           RedirectAttributes redirectAttributes,
                           @RequestHeader(value = "Referer") String referer,
                           @SessionAttribute(value = "userFolder") String userFolder) {
        minioService.copy(userFolder, currentName, copyName, type);
        addRedirectAttributes(redirectAttributes, "copy", List.of(getName(currentName, type)));
        return "redirect:" + referer;
    }

    @GetMapping("downloadFile")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("fileName") String filePath) {
        var stream = minioService.downloadFile(filePath);
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" +
                URLEncoder.encode(getFileName(filePath, true), StandardCharsets.UTF_8));
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }

    @GetMapping("downloadFolder")
    public ResponseEntity<byte[]> downloadFolder(@RequestParam("folderName") String folderPath) {
        var zipData = minioService.downloadFolder(folderPath);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", UriEncoder.encode(getNameFolder(folderPath)) + ".zip");
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(zipData.toByteArray());
    }

    private void addRedirectAttributes(RedirectAttributes redirectAttributes, String typeMessage, List<String> files) {
        redirectAttributes.addFlashAttribute("typeMessage", typeMessage);
        redirectAttributes.addFlashAttribute("files", files);
    }

    private String getName(String path, String type) {
        return type.equals("folder") ? getNameFolder(path) : getFileName(path, true);
    }

    private String getFilePath(String referer, MultipartFile file) {
        var count = 1;
        var filePath = UriEncoder.decode(getPathParam(referer)) + "/" + file.getOriginalFilename();
        var newFilePath = filePath;
        while (minioService.isFileExist(newFilePath)) {
            newFilePath = filePath.substring(0, filePath.indexOf('.')) + "(" + count++ + ")" + getFileType(file.getOriginalFilename());
        }
        return newFilePath;
    }


}
