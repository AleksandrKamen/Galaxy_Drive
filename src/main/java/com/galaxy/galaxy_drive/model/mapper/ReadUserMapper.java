package com.galaxy.galaxy_drive.model.mapper;

import com.galaxy.galaxy_drive.model.dto.UserReadDto;
import com.galaxy.galaxy_drive.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ReadUserMapper implements Mapper<User, UserReadDto>{
    @Override
    public UserReadDto map(User object) {
        return new UserReadDto(
                object.getUserName(),
                object.getFirstName(),
                object.getLastName()
        );
    }
}
