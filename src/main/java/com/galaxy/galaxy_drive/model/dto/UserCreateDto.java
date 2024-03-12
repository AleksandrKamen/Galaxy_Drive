package com.galaxy.galaxy_drive.model.dto;

import com.galaxy.galaxy_drive.model.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UserCreateDto {
    @Email
    String userName;
    @NotBlank
    String rawPassword;
    @Size(min = 3, max = 64)
    String firstName;
    @Size(min = 3, max = 64)
    String lastName;
    Role role;

}
