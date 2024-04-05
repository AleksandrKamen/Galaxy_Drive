package com.galaxy.galaxy_drive.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingControllerAspect {

    @AfterReturning(pointcut = "com.galaxy.galaxy_drive.aop.CommonAspect.isGetMethod()", returning = "pageName")
    public void addLogingGetMappingMethods(String pageName) {
        log.info("Processing {} page", pageName);
    }

    @AfterReturning(pointcut = "com.galaxy.galaxy_drive.aop.CommonAspect.isPostMethod()", returning = "redirectPageName")
    public void addLogingPostMappingMethods(JoinPoint joinPoint ,String redirectPageName) {
        log.info("method {} invoke {} page", joinPoint.getSignature().getName(),  redirectPageName);
    }
}
