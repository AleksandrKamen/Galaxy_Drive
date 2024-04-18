package com.galaxy.galaxy_drive.controller;

import com.galaxy.galaxy_drive.model.dto.minio.FileDto;
import com.galaxy.galaxy_drive.model.dto.minio.FolderDto;
import com.galaxy.galaxy_drive.service.FileStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import static com.galaxy.galaxy_drive.util.FileStorageUtil.*;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@SessionAttributes({"user", "userFolder"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileStorageController {
    FileStorageService fileStorageService;
    static String ZIP_FORMAT = ".zip";

    @PostMapping("upload")
    public String upload(@RequestParam("files")
                         List<MultipartFile> files,
                         RedirectAttributes redirectAttributes,
                         @RequestHeader(value = "Referer") String referer,
                         @SessionAttribute(value = "userFolder") String userFolder) {
        addRedirectAttributes(redirectAttributes, "upload", fileStorageService.uploadFiles(files, referer, userFolder));
        return "redirect:" + referer;
    }

    @PostMapping("create")
    public String create(String folderName,
                         RedirectAttributes redirectAttributes,
                         @RequestHeader(value = "Referer") String referer) {
        fileStorageService.createEmptyFolder(getPathParam(referer), folderName);
        addRedirectAttributes(redirectAttributes, "create", List.of(folderName));
        return "redirect:" + referer;
    }

    @PostMapping("delete")
    public String delete(String type,
                         String objectName,
                         RedirectAttributes redirectAttributes,
                         @RequestHeader(value = "Referer") String referer) {
        fileStorageService.delete(objectName, type);
        addRedirectAttributes(redirectAttributes, "delete", List.of(getNameByType(objectName, type)));
        if (!referer.contains("query") && !fileStorageService.isFolderExist(getPathParam(referer))) {
            return "redirect:" + getParentFolderPath(referer);
        }
        return "redirect:" + referer;
    }

    @PostMapping("rename")
    public String rename(String type,
                         String currentName,
                         String newName,
                         RedirectAttributes redirectAttributes,
                         @RequestHeader(value = "Referer") String referer) {
        fileStorageService.rename(currentName, newName, type);
        addRedirectAttributes(redirectAttributes, "rename", List.of(getNameByType(currentName, type)));
        return "redirect:" + referer;
    }

    @PostMapping("copy")
    public String copy(String type,
                       String currentName,
                       String copyName,
                       RedirectAttributes redirectAttributes,
                       @RequestHeader(value = "Referer") String referer,
                       @SessionAttribute(value = "userFolder") String userFolder) {
        fileStorageService.copy(userFolder, currentName, copyName, type);
        addRedirectAttributes(redirectAttributes, "copy", List.of(getNameByType(currentName, type)));
        return "redirect:" + referer;
    }

    @GetMapping("downloadFile")
    public ResponseEntity<InputStreamResource> downloadFile(FileDto file) {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + encodeString(file.getName()))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(fileStorageService.downloadObject(file.getPath())));
    }

    @GetMapping("downloadFolder")
    public ResponseEntity<byte[]> downloadFolder(FolderDto folder) {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + encodeString(folder.getName() + ZIP_FORMAT))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileStorageService.downloadFolder(folder.getPath()).toByteArray());
    }

    private void addRedirectAttributes(RedirectAttributes redirectAttributes, String typeMessage, List<String> files) {
        redirectAttributes.addFlashAttribute("typeMessage", typeMessage);
        redirectAttributes.addFlashAttribute("files", files);
    }
}
