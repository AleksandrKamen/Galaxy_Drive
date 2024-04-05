package com.galaxy.galaxy_drive.aop;

import com.galaxy.galaxy_drive.exception.user.UserAlreadyExistsException;
import com.galaxy.galaxy_drive.model.dto.user.UserCreateDto;
import com.galaxy.galaxy_drive.model.entity.user.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Aspect
@Component
@Slf4j
public class LoggingServiceAspect {

    //UserService
    @AfterReturning(pointcut = "com.galaxy.galaxy_drive.aop.CommonAspect.isCreateUserIfNotExistMethod()", returning = "user")
    public void addLogingAfterReturningCreateUserAndFolderIfNotExist(Optional<User> user) {
        if (user.isPresent()) {
            log.info("User with user name {} created, signup method - {}", user.get().getUserName(), user.get().getSignupMethod().name());
        }
    }

    @AfterThrowing(pointcut = "com.galaxy.galaxy_drive.aop.CommonAspect.isCreateUserIfNotExistMethod()", throwing = "ex")
    public void addLogingAfterThrowingCreateUserAndFolderIfNotExist(Exception ex) {
        log.error("Error occurred while create user folder: {}", ex.getMessage());
    }


    @AfterReturning("com.galaxy.galaxy_drive.aop.CommonAspect.isCreateUserMethod(userCreateDto)")
    public void addLogingAfterReturningCreateUser(UserCreateDto userCreateDto) {
        log.info("User with email {} created, signup method - Email", userCreateDto.getUserName());
    }

    @AfterThrowing(pointcut = "com.galaxy.galaxy_drive.aop.CommonAspect.isCreateUserMethod(userCreateDto)", throwing = "ex")
    public void addLogingAfterThrowingCreateUser(UserCreateDto userCreateDto, Exception ex) {
        if (ex instanceof UserAlreadyExistsException) {
            log.error("User with email {} already exists", userCreateDto.getUserName());
        } else {
            log.error("Error occurred while create user folder: {}", ex.getMessage());
        }
    }

    @AfterReturning("com.galaxy.galaxy_drive.aop.CommonAspect.isUserServise() && execution(public * updateUser(String,String,String)) && args(userName,  name, lastName)")
    public void addLogingAfterUpdateUser(String userName, String name, String lastName) {
        log.info("User with userName {} was update: new first name - {}, new last name - {}", userName, name, lastName);
    }

    @AfterReturning("com.galaxy.galaxy_drive.aop.CommonAspect.isFindByUserNameMethod(userName))")
    public void addLogingAfterReturningFindByUser(String userName) {
        log.info("User with userName {} found", userName);
    }

    @AfterThrowing("com.galaxy.galaxy_drive.aop.CommonAspect.isFindByUserNameMethod(userName))")
    public void addLogingAfterThrowingFindByUser(String userName) {
        log.error("User with userName {} not found, redirect to error page", userName);
    }

    ///MinioService

    @AfterReturning("com.galaxy.galaxy_drive.aop.CommonAspect.isDeleteMethod(fileName, type)")
    public void addLogingAfterDeleteMinioObject(String fileName, String type) {
        log.info("{} with path {} was delete", type, fileName);
    }

    @AfterThrowing(pointcut = "com.galaxy.galaxy_drive.aop.CommonAspect.isDeleteMethod(fileName, type)", throwing = "ex")
    public void addLogingAfterThrowingCopyMinioObject(String fileName, String type, Exception ex) {
        log.error("error occurred while delete {} with path {}: {}", type, fileName, ex.getMessage());
    }

    @AfterReturning("com.galaxy.galaxy_drive.aop.CommonAspect.isCopyMethod(currentName, copyName, type)")
    public void addLogingAfterCopyMinioObject(String currentName, String copyName, String type) {
        log.info("create copy {} with path {} - copy name {}", type, currentName, copyName);
    }

    @AfterThrowing(pointcut = "com.galaxy.galaxy_drive.aop.CommonAspect.isCopyMethod(currentName, copyName, type)", throwing = "ex")
    public void addLogingAfterThrowingCopyMinioObject(String currentName, String copyName, String type, Exception ex) {
        log.error("error occurred while copy {} with path {}, copy name - {}: {}", type, currentName, copyName, ex.getMessage());
    }

    @AfterReturning("com.galaxy.galaxy_drive.aop.CommonAspect.isRenameMethod(currentName, newName, type)")
    public void addLogingAfterRenameMinioObject(String currentName, String newName, String type) {
        log.info("rename {} with path {} - new name {}", type, currentName, newName);
    }

    @AfterThrowing(pointcut = "com.galaxy.galaxy_drive.aop.CommonAspect.isRenameMethod(currentName, newName, type)", throwing = "ex")
    public void addLogingAfterThrowingRenameMinioObject(String currentName, String newName, String type, Exception ex) {
        log.error("error occurred while rename {} with path {}, new name - {}: {}", type, currentName, newName, ex.getMessage());
    }


    @AfterReturning("com.galaxy.galaxy_drive.aop.CommonAspect.isUploadFileMethod(file, path)")
    public void addLogingAfterUploadMinioObject(MultipartFile file, String path) {
        log.info("file with name {} was uploaded, path - {}", file.getOriginalFilename(), path);
    }

    @AfterThrowing(pointcut = "com.galaxy.galaxy_drive.aop.CommonAspect.isUploadFileMethod(file,path)", throwing = "ex")
    public void addLogingAfterThrowingUploadMinioObject(MultipartFile file, String path, Exception ex) {
        log.error("error occurred while uploading file with name {} to path {}: {}", file.getOriginalFilename(), path, ex.getMessage());
    }

    @AfterReturning("com.galaxy.galaxy_drive.aop.CommonAspect.isDownloadMethod() && args(path)")
    public void addLogingAfterDownloadMinioObject(String path) {
        log.info("file/folder with path {} was download", path);
    }

    @AfterThrowing(pointcut = "com.galaxy.galaxy_drive.aop.CommonAspect.isDownloadMethod() && args(path)", throwing = "ex")
    public void addLogingAfterThrowingDownloadMinioObject(String path, Exception ex) {
        log.error("error occurred while downloading file/folder with path {}: {}", path, ex.getMessage());
    }

    @AfterReturning("com.galaxy.galaxy_drive.aop.CommonAspect.isCreateFolderMethod(parentFolder, folderName)")
    public void addLogingAfterCreateFolder(String parentFolder,  String folderName) {
        log.info("folder with name {} was created, parent folder: {}", folderName, parentFolder);
    }

    @AfterThrowing(pointcut = "com.galaxy.galaxy_drive.aop.CommonAspect.isCreateFolderMethod(parentFolder, folderName)", throwing = "ex")
    public void addLogingAfterThrowingCreateFolder(String parentFolder,  String folderName, Exception ex) {
        log.error("error occurred while create folder with name {} in to {}: {}", folderName, parentFolder,  ex.getMessage());
    }



}

