package com.galaxy.galaxy_drive.model.mapper;

import com.galaxy.galaxy_drive.model.dto.UserCreateDto;
import com.galaxy.galaxy_drive.model.dto.UserReadDto;
import com.galaxy.galaxy_drive.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public abstract class UserMapper {
    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Mapping(source = "rawPassword", target = "password", qualifiedByName = "mapPassword")
    @Mapping( target = "id", ignore = true)
    public abstract User userCreateDtoToUser(UserCreateDto userCreateDto);

    public abstract UserReadDto userToUserReadDto(User user);

    @Named("mapPassword")
    public String mapPassword(String rawPassword) {
        var encode = passwordEncoder.encode(rawPassword);
        System.out.println(encode);
        return encode;
    }

}
