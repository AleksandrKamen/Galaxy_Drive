package com.galaxy.galaxy_drive.service.minio;

import com.galaxy.galaxy_drive.exception.minio.*;
import com.galaxy.galaxy_drive.model.dto.minio.MinioFileDto;
import com.galaxy.galaxy_drive.model.dto.minio.MinioFolderDto;
import com.galaxy.galaxy_drive.model.mapper.minio.MinioMapper;
import com.galaxy.galaxy_drive.model.repository.MinioRepository;
import io.minio.GetObjectResponse;
import io.minio.messages.Item;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.util.UriEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.galaxy.galaxy_drive.util.FileUtil.*;
import static com.galaxy.galaxy_drive.util.FolderUtil.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinioService {
    MinioMapper minioMapper;
    MessageSource messageSource;
    MinioRepository minioRepository;
    static Long MAX_MEMORY = 1073741824L;

//    ALL
    public void delete(String fileName, String type) {
        switch (type) {
            case "file" -> removeObject(fileName);
            case "folder" -> minioRepository.getAllObjectInFolder(fileName, true).stream()
                    .forEach(it -> removeObject(it.objectName()));
        }
    }

    public void copy(String userFolder, String currentName, String copyName, String type) {
        if (!isNameCorrect(copyName)) {
            throw new IncorrectNameException(getErrorMessage("incorrect.name"));
        }
        checkFileFitOnDisk(userFolder, getObjectSize(copyName, type));

        switch (type) {
            case "file" -> copyFile(currentName, copyName);
            case "folder" -> copyFolder(currentName, copyName);
        }
    }

    public void rename(String currentName, String newName, String type) {
        if (!isNameCorrect(newName)) {
            throw new IncorrectNameException(getErrorMessage("incorrect.name"));
        }
        switch (type) {
            case "file" -> copyFile(currentName, newName);
            case "folder" -> copyFolder(currentName, newName);
        }
        delete(currentName, type);
    }

    // File
    public List<MinioFileDto> getAllFilesInFolder(String folderName) {
        return minioRepository.getAllObjectInFolder(folderName, false)
                .stream()
                .filter(item -> !item.isDir())
                .filter(item -> !item.objectName().equals(folderName + "/"))
                .map(minioMapper::itemToMinioFileDto)
                .toList();
    }

    public void uploadFile(MultipartFile file, String path) {
        try{
            minioRepository.putObject(path, file.getInputStream(), file.getSize());
    } catch (Exception e) {
        throw new MinioUploadException(getErrorMessage("upload") + e.getMessage());
    }
    }

    public GetObjectResponse downloadFile(String fileName) {
        try {
            return minioRepository.downloadObject(fileName);
    } catch (Exception e) {
        throw new MinioDownloadException(getErrorMessage("download") + e.getMessage());
    }
    }

    public List<MinioFileDto> searchFileByName(String folderName, String fileName) {
        return minioRepository.getAllObjectInFolder(folderName, true).stream()
                .filter(item -> !item.objectName().endsWith("/"))
                .map(minioMapper::itemToMinioFileDto)
                .filter(file -> file.getName().startsWith(fileName))
                .collect(Collectors.toList());
    }

    private boolean isFileExist(String filePath) {
        try {
        return minioRepository.isObjectExist(filePath);
    } catch (Exception e) {
        return false;
    }
    }

    private void copyFile(String currentName, String newName) {
        var newFilePath = getParentFolderPath(currentName) + "/" + newName + getFileType(currentName);
        if (getFileName(currentName, false).equals(newName) || isFileExist(newFilePath)) {
            throw new MinioDuplicateNameException(getErrorMessage("duplicate"));
        }
        try {
            minioRepository.copyObject(currentName, newFilePath);
        } catch (Exception e) {
            throw new MinioCopyException(getErrorMessage("copy") + e.getMessage());
        }
    }


//    Folder

    public List<MinioFolderDto> getAllFoldersInFolder(String folderName) {
        return minioRepository.getAllObjectInFolder(folderName, false)
                .stream()
                .filter(item -> item.isDir())
                .map(item -> minioMapper.stringToMinioFolderDto(item.objectName()))
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
        try {
            minioRepository.putObject(getUserFolderName(id) + "/", new ByteArrayInputStream(new byte[0]), 0L);
        }
        catch (Exception e) {
            throw new MinioCreateException(getErrorMessage("createFolder") + e.getMessage());
        }
    }

    public void createEmptyFolder(String parentFolder, String folderName) {

        if (!isNameCorrect(folderName)) {
            throw new IncorrectNameException(getErrorMessage("incorrect.name"));
        }
        var folderPath = parentFolder + "/" + folderName;

        if (isFolderExist(folderPath)) {
            throw new MinioDuplicateNameException(getErrorMessage("duplicate"));
        }

        try {
            minioRepository.putObject(folderPath + "/", new ByteArrayInputStream(new byte[0]), 0L);
        } catch (Exception e) {
            throw new MinioUploadException(getErrorMessage("upload") + e.getMessage());
        }
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
            throw new MinioDownloadException(getErrorMessage("download"));
        }

    }

    public List<MinioFolderDto> searchFolderByName(String parentFolderName, String folderName) {
        return getAllUsersFolders(parentFolderName).stream()
                .map(minioMapper::stringToMinioFolderDto)
                .filter(folder -> folder.getName().startsWith(folderName))
                .collect(Collectors.toList());
    }

    public Boolean isFolderExist(String folderPath) {
        return !minioRepository.getAllObjectInFolder(folderPath, true).isEmpty();
    }

    private void copyFolder(String folderPath, String newName) {
        var newPathFolder = getNewPathFolder(folderPath, newName);
        if (folderPath.equals(newPathFolder) || isFolderExist(newPathFolder)) {
            throw new MinioDuplicateNameException(getErrorMessage("duplicate"));
        }
        try {
            minioRepository.getAllObjectInFolder(folderPath, true).stream()
                    .forEach(it-> minioRepository.copyObject(it.objectName(), it.objectName().replace(folderPath,newPathFolder)));
        } catch (Exception e) {
            throw new MinioCopyException(getErrorMessage("copy") + e.getMessage());
        }
    }
//    ALL

    public Long getUsedMemory(String userFolder) {
        return minioRepository.getAllObjectInFolder(userFolder, true).stream()
                .mapToLong(it -> it.size())
                .sum();
    }

    public void checkFileFitOnDisk(String userFolder, long totalSize) {
        if (!(MAX_MEMORY - getUsedMemory(userFolder) > totalSize)) {
            throw new MemoryLlimitException(getErrorMessage("memory.limit"));
        }
    }

    public String getProcessText(String userFolder) {
        return messageSource.getMessage(
                "process.text",
                new String[]{getFileSize(getUsedMemory(userFolder)), getFileSize(MAX_MEMORY)},
                LocaleContextHolder.getLocale());
    }


    public Long getObjectSize(String path, String type) {
            return switch (type){
                case "file" -> minioRepository.getObjectSize(path);
                case "folder" -> minioRepository.getAllObjectInFolder(path, true)
                        .stream()
                        .mapToLong(item -> item.size())
                        .sum();
                default -> throw new IllegalStateException("Unexpected value: " + type);
            };
    }

    public Long getPercentUsedMemory(String userFolder) {
        return getUsedMemory(userFolder) * 100 / MAX_MEMORY;
    }

    public String getFileCopyPath(String referer, MultipartFile file) {
        var count = 1;
        var filePath = UriEncoder.decode(getPathParam(referer)) + "/" + file.getOriginalFilename();
        var newFilePath = filePath;
        while (isFileExist(newFilePath)) {
            newFilePath = filePath.substring(0, filePath.indexOf('.')) + "(" + count++ + ")" + getFileType(file.getOriginalFilename());
        }
        return newFilePath;
    }

    private void removeObject(String fileName) {
        try {
        minioRepository.removeObject(fileName);
    } catch (Exception e) {
        throw new MinioRemoveException(getErrorMessage("remove") + e.getMessage());
    }
    }

    private boolean isNameCorrect(String name) {
        return name.matches("^[\\p{L}0-9_-]{1,30}$");
    }


    private String getErrorMessage(String errorType) {
        return messageSource.getMessage("error.message." + errorType, null, LocaleContextHolder.getLocale());
    }


}
