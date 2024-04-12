package com.galaxy.galaxy_drive.model.mapper.user;

import com.galaxy.galaxy_drive.model.dto.user.UserCreateDto;
import com.galaxy.galaxy_drive.model.dto.user.UserReadDto;
import com.galaxy.galaxy_drive.model.entity.user.Role;
import com.galaxy.galaxy_drive.model.entity.user.SignupMethod;
import com.galaxy.galaxy_drive.model.entity.user.User;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public abstract class UserMapper {
    @Autowired
    protected PasswordEncoder passwordEncoder;

    public abstract UserReadDto userToUserReadDto(User user);

    public User userCreateDtoToUser(UserCreateDto userCreateDto){
        return User.builder()
                .userName(userCreateDto.getUserName())
                .password(passwordEncoder.encode(userCreateDto.getRawPassword()))
                .role(Role.USER)
                .signupMethod(SignupMethod.EMAIL)
                .build();
    }

}
