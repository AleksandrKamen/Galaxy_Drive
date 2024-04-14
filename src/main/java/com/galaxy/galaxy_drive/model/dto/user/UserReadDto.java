package com.galaxy.galaxy_drive.model.dto.user;

import lombok.Value;

import java.io.Serializable;

@Value
public class UserReadDto implements Serializable {
    Long id;
    String userName;
    String firstName;
    String lastName;
}
