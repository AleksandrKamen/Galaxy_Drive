package com.galaxy.galaxy_drive.controller;

import com.galaxy.galaxy_drive.exception.minio.FolderNotFoundException;
import com.galaxy.galaxy_drive.service.minio.MinioService;
import com.galaxy.galaxy_drive.service.user.UserService;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
     MessageSource messageSource;

    @GetMapping
    public String homePage(@RequestParam(required = false) String path,
                           @AuthenticationPrincipal Object principal,
                           Model model) {
        String userName= null;
        if (principal instanceof UserDetails){
            userName =  ((UserDetails) principal).getUsername();
        } else if (principal instanceof OAuth2User){
            var email = ((OAuth2User) principal).getAttribute("email");
            var login = ((OAuth2User) principal).getAttribute("login");
            userName = email != null ? email.toString() : login != null ? login.toString() : null;
        }
        var user = userService.findByUserName(userName);
        var userFolderPath = FolderUtil.getUserFolderName(user.getId());

        if (path == null || path.isEmpty()) {
            return "redirect:/?path=" + userFolderPath;
        }

        if (!path.startsWith(userFolderPath) || !minioService.isFolderExist(path)){
            throw new FolderNotFoundException(messageSource.getMessage("error.message.folder", null, LocaleContextHolder.getLocale()));
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
        var path = referer.substring(referer.indexOf("=") + 1);
        var addFiles = files.stream()
                            .map(file -> {
                                minioService.uploadFile(file, UriEncoder.decode(path) + "/" + file.getOriginalFilename());
                                return file.getOriginalFilename();
                            })
                            .collect(Collectors.toList());
        addRedirectAttributes(redirectAttributes, "upload", addFiles);
        return "redirect:/?path="+path;
    }

    @PostMapping("create")
    public String createFolder(String folderName,
                               RedirectAttributes redirectAttributes,
                               @RequestHeader(value = "Referer") String referer)  {
        var indexOf = referer.indexOf("=") + 1;
        var path = referer.substring(indexOf);
        minioService.createEmptyFolderWithName(path, folderName);
        addRedirectAttributes(redirectAttributes,"create", List.of(folderName));
        return "redirect:" + referer;
    }

    @PostMapping("delete")
    public String deleteFile(String type,
                             String objectName,
                             RedirectAttributes redirectAttributes,
                             @RequestHeader(value = "Referer") String referer){
        minioService.delete(objectName,type);
        addRedirectAttributes(redirectAttributes, "delete", List.of(objectName));
        if (!minioService.isFolderExist(referer.substring(referer.indexOf("=") + 1))){
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
        addRedirectAttributes(redirectAttributes, "rename", List.of(currentName));
        return "redirect:" + referer;
    }

    @PostMapping("copy")
    public String copyFile(String type,
                           String currentName,
                           String copyName,
                           RedirectAttributes redirectAttributes,
                           @RequestHeader(value = "Referer") String referer)  {
        minioService.copy(currentName, copyName, type);
        addRedirectAttributes(redirectAttributes, "copy", List.of(currentName));
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
}
