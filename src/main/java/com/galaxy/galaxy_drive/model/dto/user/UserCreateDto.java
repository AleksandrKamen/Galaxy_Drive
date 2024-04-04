package com.galaxy.galaxy_drive.model.dto.user;

import com.galaxy.galaxy_drive.validation.FieldEquals;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
@FieldEquals (field="rawPassword", equalsTo="confirmPassword", message = "{password.notMatches}")
public class UserCreateDto {
    @Email
    String userName;
    @NotBlank(message = "{password.blank.message}")
    @Size(min = 3, max = 10, message = "{password.size.message}")
    String rawPassword;
    @NotBlank (message = "{confirm.password.blank.message}")
    @Size(min = 3, max = 10, message = "{confirm.password.size.message}")
    String confirmPassword;
}
