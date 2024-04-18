package com.galaxy.galaxy_drive.service;

import com.galaxy.galaxy_drive.exception.minio.IncorrectNameException;
import com.galaxy.galaxy_drive.exception.minio.MemoryLlimitException;
import com.galaxy.galaxy_drive.model.dto.minio.FileDto;
import com.galaxy.galaxy_drive.model.dto.minio.FolderDto;
import com.galaxy.galaxy_drive.model.repository.MinioRepository;
import com.galaxy.galaxy_drive.service.minio.MinioService;
import io.minio.GetObjectResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.util.List;
import static com.galaxy.galaxy_drive.util.FileStorageUtil.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileStorageService {
    MessageService messageService;
    MinioRepository minioRepository;
    MinioService minioService;


    public void delete(String fileName, String type) {
        switch (type) {
            case "file" -> minioRepository.removeObject(fileName);
            case "folder" -> minioRepository.getAllObjectInFolder(fileName, true).stream()
                    .forEach(it -> minioRepository.removeObject(it.objectName()));
        }
    }

    public void copy(String userFolder, String currentName, String copyName, String type) {
        if (!isNameCorrect(copyName)) {
            throw new IncorrectNameException(messageService.getErrorMessage("incorrect.name"));
        }
        if (!minioRepository.isFileFitOnDisk(userFolder, minioRepository.getObjectSizeByType(currentName, type))) {
            throw new MemoryLlimitException(messageService.getErrorMessage("memory.limit"));
        }
        switch (type) {
            case "file" -> minioService.copyFile(currentName, copyName.trim());
            case "folder" -> minioService.copyFolder(currentName, copyName.trim());
        }
    }

    public void rename(String currentName, String newName, String type) {
        if (!isNameCorrect(newName)) {
            throw new IncorrectNameException(messageService.getErrorMessage("incorrect.name"));
        }
        switch (type) {
            case "file" -> minioService.copyFile(currentName, newName.trim());
            case "folder" -> minioService.copyFolder(currentName, newName.trim());
        }
        delete(currentName, type);
    }

    public List<String> uploadFiles(List<MultipartFile> files, String referer, String userFolder) {
        return minioService.uploadFiles(files, referer, userFolder);
    }

    public void createEmptyFolder(String parentFolder, String folderName) {
        minioService.createEmptyFolder(parentFolder, folderName);
    }

    public GetObjectResponse downloadObject(String fileName) {
        return minioRepository.downloadObject(fileName);
    }

    public ByteArrayOutputStream downloadFolder(String folderName) {
        return minioService.downloadFolder(folderName);
    }

    public Boolean isFolderExist(String folderPath) {
        return !minioRepository.getAllObjectInFolder(folderPath, true).isEmpty();
    }

    public List<FolderDto> getAllFoldersInFolder(String folderName) {
        return minioService.getAllFoldersInFolder(folderName);
    }

    public List<FileDto> getAllFilesInFolder(String folderName) {
        return minioService.getAllFilesInFolder(folderName);
    }

    public String getMemoryUsedText(String userFolder) {
        return minioRepository.getMemoryUsedText(userFolder);
    }

    public Long getPercentUsedMemory(String userFolder) {
        return minioRepository.getPercentUsedMemory(userFolder);
    }

    public List<FolderDto> searchFolderByName(String parentFolderName, String folderName) {
        return minioService.searchFolderByName(parentFolderName, folderName);
    }

    public List<FileDto> searchFileByName(String folderName, String fileName) {
        return minioService.searchFileByName(folderName, fileName);
    }

}
