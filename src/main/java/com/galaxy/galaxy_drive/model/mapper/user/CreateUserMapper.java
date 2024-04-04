package com.galaxy.galaxy_drive.model.mapper.user;

import com.galaxy.galaxy_drive.model.dto.user.UserCreateDto;
import com.galaxy.galaxy_drive.model.entity.user.Role;
import com.galaxy.galaxy_drive.model.entity.user.SignupMethod;
import com.galaxy.galaxy_drive.model.entity.user.User;
import com.galaxy.galaxy_drive.model.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateUserMapper implements Mapper<UserCreateDto, User> {
    private final PasswordEncoder passwordEncoder;

    @Override
    public User map(UserCreateDto object) {
        return User.builder()
                .userName(object.getUserName())
                .password(passwordEncoder.encode(object.getRawPassword()))
                .role(Role.USER)
                .signupMethod(SignupMethod.EMAIL)
                .build();
    }
}
