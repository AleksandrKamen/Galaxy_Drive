package com.galaxy.galaxy_drive.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class UserCreateDto {
    @Email
    String userName;
    @NotBlank
    String rawPassword;
    @NotBlank
    String confirmPassword;

}
