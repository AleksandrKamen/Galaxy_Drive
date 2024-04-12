package com.galaxy.galaxy_drive.controller;

import com.galaxy.galaxy_drive.exception.minio.FolderNotFoundException;
import com.galaxy.galaxy_drive.service.minio.MinioService;
import com.galaxy.galaxy_drive.service.user.UserService;
import com.galaxy.galaxy_drive.util.AuthenticationUtil;
import com.galaxy.galaxy_drive.util.FileUtil;
import com.galaxy.galaxy_drive.util.FolderUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IndexController {
     UserService userService;
     MinioService minioService;

    @GetMapping
    public String homePage(@RequestParam(required = false) String path,
                           @AuthenticationPrincipal Object principal,
                           Model model) {
        var user = userService.findByUserName(AuthenticationUtil.getUserName(principal));
        var userFolderPath = FolderUtil.getUserFolderName(user.getId());

        if (path == null || path.isEmpty()) {
            return "redirect:/?path=" + userFolderPath;
        }

        if (!path.startsWith(userFolderPath) || !minioService.isFolderExist(path)){
            throw new FolderNotFoundException("Folder not found or not exist");
        }

           model.addAllAttributes(Map.of(
                "user", user,
                "allFoldersInFolder", minioService.getAllFoldersInFolder(path),
                "allFilesInFolder", minioService.getAllFilesInFolder(path),
                "parentFolders", FolderUtil.getBreadcrumbs(path),
                "userFolderName", userFolderPath,
                "path",path
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
                             @RequestHeader(value = "Referer") String referer) {
        var uploadFiles = files.stream()
                            .map(file -> {
                                minioService.uploadFile(file, UriEncoder.decode(getPath(referer)) + "/" + file.getOriginalFilename());
                                return file.getOriginalFilename();
                            })
                            .collect(Collectors.toList());
        addRedirectAttributes(redirectAttributes, "upload", uploadFiles);
        return "redirect:" + referer;
    }

    @PostMapping("create")
    public String createFolder(String folderName,
                               RedirectAttributes redirectAttributes,
                               @RequestHeader(value = "Referer") String referer)  {
        minioService.createEmptyFolderWithName(getPath(referer), folderName);
        addRedirectAttributes(redirectAttributes,"create", List.of(folderName));
        return "redirect:" + referer;
    }

    @PostMapping("delete")
    public String deleteFile(String type,
                             String objectName,
                             RedirectAttributes redirectAttributes,
                             @RequestHeader(value = "Referer") String referer){
        minioService.delete(objectName,type);
        addRedirectAttributes(redirectAttributes, "delete", List.of(getName(objectName,type)));
        if (!minioService.isFolderExist(getPath(referer))){
            return "redirect:" + FolderUtil.getParentFolderPath(referer);
        }
        return "redirect:" + referer;
    }
    @PostMapping("rename")
    public String renameFile(String type,
                             String currentName,
                             String newName,
                             RedirectAttributes redirectAttributes,
                             @RequestHeader(value = "Referer") String referer) {
        minioService.rename(currentName,newName,type);
        addRedirectAttributes(redirectAttributes, "rename", List.of(getName(currentName, type)));
        return "redirect:" + referer;
    }

    @PostMapping("copy")
    public String copyFile(String type,
                           String currentName,
                           String copyName,
                           RedirectAttributes redirectAttributes,
                           @RequestHeader(value = "Referer") String referer)  {
        minioService.copy(currentName, copyName, type);
        addRedirectAttributes(redirectAttributes, "copy", List.of(getName(currentName, type)));
        return "redirect:" + referer;
    }


    @GetMapping("downloadFile")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("fileName") String fileName) {
            var stream = minioService.downloadFile(fileName);
            var headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(stream));
    }
    @GetMapping("downloadFolder")
    public ResponseEntity<byte[]> downloadFolder(@RequestParam("folderName") String folderName) {
            var zipData = minioService.downloadFolder(folderName);
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", FolderUtil.getNameFolder(folderName) + ".zip");
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(zipData.toByteArray());
    }
    private void addRedirectAttributes(RedirectAttributes redirectAttributes, String typeMessage, List<String> files){
        redirectAttributes.addFlashAttribute("typeMessage",typeMessage);
        redirectAttributes.addFlashAttribute("files",files);
    }
    private String getName(String path, String type){
        return type.equals("folder") ? FolderUtil.getNameFolder(path) : FileUtil.getFileNameWithType(path);
    }
    private String getPath(String referer){
        return referer.substring(referer.indexOf("=") + 1);
    }


}
