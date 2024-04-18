package com.galaxy.galaxy_drive.service.minio;

import com.galaxy.galaxy_drive.exception.minio.IncorrectNameException;
import com.galaxy.galaxy_drive.exception.minio.MemoryLlimitException;
import com.galaxy.galaxy_drive.exception.minio.MinioDownloadException;
import com.galaxy.galaxy_drive.exception.minio.MinioDuplicateNameException;
import com.galaxy.galaxy_drive.model.dto.minio.FileDto;
import com.galaxy.galaxy_drive.model.dto.minio.FolderDto;
import com.galaxy.galaxy_drive.model.mapper.minio.FileMapper;
import com.galaxy.galaxy_drive.model.repository.MinioRepository;
import com.galaxy.galaxy_drive.service.MessageService;
import io.minio.messages.Item;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static com.galaxy.galaxy_drive.util.FileStorageUtil.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinioService {
    FileMapper minioMapper;
    MessageService messageService;
    MinioRepository minioRepository;


    public List<FileDto> getAllFilesInFolder(String folderName) {
        return minioRepository.getAllObjectInFolder(folderName, false)
                .stream()
                .filter(item -> !item.isDir())
                .filter(item -> !item.objectName().equals(folderName + "/"))
                .map(minioMapper::itemToFileDto)
                .toList();
    }

    public List<String> uploadFiles(List<MultipartFile> files, String referer, String userFolder) {
        if (!minioRepository.isFileFitOnDisk(userFolder, files.stream().mapToLong(file -> file.getSize()).sum())) {
            throw new MemoryLlimitException(messageService.getErrorMessage("memory.limit"));
        }
        return files.stream()
                .map(file -> {
                    minioRepository.uploadFile(file, minioRepository.generateCopyFilePath(referer, file));
                    return file.getOriginalFilename();
                })
                .collect(Collectors.toList());
    }

    public List<FileDto> searchFileByName(String folderName, String fileName) {
        return minioRepository.getAllObjectInFolder(folderName, true).stream()
                .filter(item -> !item.objectName().endsWith("/"))
                .map(minioMapper::itemToFileDto)
                .filter(file -> file.getName().startsWith(fileName))
                .collect(Collectors.toList());
    }

    public List<FolderDto> getAllFoldersInFolder(String folderName) {
        return minioRepository.getAllObjectInFolder(folderName, false)
                .stream()
                .filter(item -> item.isDir())
                .map(item -> minioMapper.stringToFolderDto(item.objectName()))
                .toList();
    }

    public Set<String> getAllUsersFolders(String parentFolderName) {
        return minioRepository.getAllObjectInFolder(parentFolderName, true).stream()
                .map(item -> item.objectName())
                .filter(path -> path.indexOf("/") != path.lastIndexOf("/"))
                .map(path -> path.substring(0, path.lastIndexOf("/") + 1))
                .collect(Collectors.toSet());
    }

    public void createUserFolder(Long id) {
        minioRepository.putObject(getUserFolderName(id) + "/", new ByteArrayInputStream(new byte[0]), 0L);
    }

    public void createEmptyFolder(String parentFolder, String folderName) {

        if (!isNameCorrect(folderName)) {
            throw new IncorrectNameException(messageService.getErrorMessage("incorrect.name"));
        }
        var folderPath = parentFolder + "/" + folderName;

        if (isFolderExist(folderPath)) {
            throw new MinioDuplicateNameException(messageService.getErrorMessage("duplicate"));
        }
        minioRepository.putObject(folderPath + "/", new ByteArrayInputStream(new byte[0]), 0L);
    }

    public ByteArrayOutputStream downloadFolder(String folderName) {
        try {
            var baos = new ByteArrayOutputStream();
            try (var outputStream = new ZipOutputStream(baos)) {
                for (Item result : minioRepository.getAllObjectInFolder(folderName, true)) {
                    var object = minioRepository.downloadObject(result.objectName());
                    outputStream.putNextEntry(new ZipEntry(result.objectName()));
                    var buffer = new byte[1024];
                    int length;
                    while ((length = object.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.closeEntry();
                }
            }
            return baos;
        } catch (Exception e) {
            throw new MinioDownloadException(messageService.getErrorMessage("download"));
        }

    }

    public List<FolderDto> searchFolderByName(String parentFolderName, String folderName) {
        return getAllUsersFolders(parentFolderName).stream()
                .map(minioMapper::stringToFolderDto)
                .filter(folder -> folder.getName().startsWith(folderName))
                .collect(Collectors.toList());
    }

    public Boolean isFolderExist(String folderPath) {
        return !minioRepository.getAllObjectInFolder(folderPath, true).isEmpty();
    }

    public void copyFile(String currentName, String newName) {
        var newFilePath = getParentFolderPath(currentName) + "/" + newName + getFileType(currentName);
        if (minioRepository.isObjectExist(newFilePath)) {
            throw new MinioDuplicateNameException(messageService.getErrorMessage("duplicate"));
        }
        minioRepository.copyObject(currentName, newFilePath);
    }

    public void copyFolder(String folderPath, String newName) {
        var newPathFolder = getNewPathFolder(folderPath, newName);
        if (isFolderExist(newPathFolder)) {
            throw new MinioDuplicateNameException(messageService.getErrorMessage("duplicate"));
        }
        minioRepository.getAllObjectInFolder(folderPath, true).stream()
                .forEach(it -> minioRepository.copyObject(it.objectName(), it.objectName().replace(folderPath, newPathFolder)));
    }
}
