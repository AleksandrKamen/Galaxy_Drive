package com.galaxy.galaxy_drive.model.dto.user;

import lombok.Value;

@Value
public class UserReadDto {
    Long id;
    String userName;
    String firstName;
    String lastName;
}
