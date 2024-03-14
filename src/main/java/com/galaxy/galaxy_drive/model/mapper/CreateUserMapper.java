package com.galaxy.galaxy_drive.model.mapper;

import com.galaxy.galaxy_drive.model.dto.UserCreateDto;
import com.galaxy.galaxy_drive.model.entity.Role;
import com.galaxy.galaxy_drive.model.entity.User;
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
                .build();
    }
}
