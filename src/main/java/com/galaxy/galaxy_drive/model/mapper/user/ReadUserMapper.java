package com.galaxy.galaxy_drive.model.mapper.user;

import com.galaxy.galaxy_drive.model.dto.user.UserReadDto;
import com.galaxy.galaxy_drive.model.entity.user.User;
import com.galaxy.galaxy_drive.model.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class ReadUserMapper implements Mapper<User, UserReadDto> {
    @Override
    public UserReadDto map(User object) {
        return new UserReadDto(
                object.getId(),
                object.getUserName(),
                object.getFirstName(),
                object.getLastName()
        );
    }
}
