package com.galaxy.galaxy_drive.util;

import com.galaxy.galaxy_drive.exception.user.UserNotFoundException;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@UtilityClass
public class AuthenticationUtil {
    public String getUserName(Object principal){
        if (principal instanceof UserDetails){
            return  ((UserDetails) principal).getUsername();
        } else if (principal instanceof OAuth2User){
            var email = ((OAuth2User) principal).getAttribute("email");
            var login = ((OAuth2User) principal).getAttribute("login");
            return email != null ? email.toString() : login != null ? login.toString() : null;
        }
        throw new UserNotFoundException("user not recognized");
    }
}
