package com.galaxy.galaxy_drive.aop;

import com.galaxy.galaxy_drive.model.dto.user.UserCreateDto;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Aspect
@Component
public class CommonAspect {

    @Pointcut("within(com.galaxy.galaxy_drive.service.user.*)")
    public void isUserServise(){}
    @Pointcut("within(com.galaxy.galaxy_drive.service.minio.*)")
    public void isMinioServise(){}
    @Pointcut("@within(org.springframework.stereotype.Controller)")
    public void isControllerLayer(){}

    @Pointcut("@within(org.springframework.stereotype.Controller) && @annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void isGetMethod(){}

    @Pointcut("@within(org.springframework.stereotype.Controller) && @annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void isPostMethod(){}

    @Pointcut("isUserServise() && execution(public * create(*)) && args(userCreateDto)")
    public void isCreateUserMethod(UserCreateDto userCreateDto) {
    }
    @Pointcut("isUserServise()  && execution(public * findByUserName(String)) && args(userName)")
    public void isFindByUserNameMethod(String userName) {
    }
    @Pointcut("isUserServise()  && execution(public * createUserIfNotExist(*)) ")
    public void isCreateUserIfNotExistMethod() {
    }
    @Pointcut("isMinioServise() && (execution(public * downloadFile(String)) || execution(public * downloadFolder(String)))")
    public void isDownloadMethod(){}
    @Pointcut("isMinioServise() && execution(public * copy(String, String, String)) && args(currentName, copyName, type)")
    public void isCopyMethod(String currentName, String copyName, String type){}
    @Pointcut("isMinioServise() && execution(public * delete(String, String)) && args(fileName, type)")
    public void isDeleteMethod(String fileName,  String type){}
    @Pointcut("isMinioServise() && execution(public * uploadFile(*,*)) && args(file, path)")
    public void isUploadFileMethod(MultipartFile file, String path) {
    }
    @Pointcut("isMinioServise() && execution(public * rename(String, String, String)) && args(currentName, newName, type)")
    public void isRenameMethod(String currentName, String newName, String type){}
    @Pointcut("isMinioServise() && execution(public * createEmptyFolderWithName(String, String)) && args(parentFolder, folderName)")
    public void isCreateFolderMethod(String parentFolder, String folderName){}



}
