package com.galaxy.galaxy_drive.service.integration;

import com.galaxy.galaxy_drive.exception.user.UserAlreadyExistsException;
import com.galaxy.galaxy_drive.model.dto.user.UserCreateDto;
import com.galaxy.galaxy_drive.model.entity.user.Role;
import com.galaxy.galaxy_drive.model.entity.user.SignupMethod;
import com.galaxy.galaxy_drive.model.repository.UserRepository;
import com.galaxy.galaxy_drive.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

class UserServiceIntegrationIT extends IntegrationTestBase {
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    static final String TEST_EMAIL = "test@gmail.com";
    static final String TEST_PASSWORD = "password";

    @BeforeEach
    void cleanup() {
        userRepository.deleteAll();
    }
    @Test
    void createUserSuccessful() {
        userService.create(new UserCreateDto(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD));
        var actualResult = userRepository.findByUserName(TEST_EMAIL);
        assertTrue(actualResult.isPresent());
        assertTrue(actualResult.get().getRole() == Role.USER);
    }

    @Test
    void createUserThrowsAlreadyExistsException() {
        var createUser = new UserCreateDto(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD);
        userService.create(new UserCreateDto(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD));
        assertThrows(UserAlreadyExistsException.class, () -> userService.create(createUser));
    }

    @Test
    void updateUser() {
        userService.create(new UserCreateDto(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD));
        userService.updateUser(TEST_EMAIL, "testName", "testLastName");
        var actualResult = userService.findByUserName(TEST_EMAIL);
        assertThat(actualResult.getFirstName()).isEqualTo("testName");
        assertThat(actualResult.getLastName()).isEqualTo("testLastName");
    }

    @Test
    void createOAuth2UserIfNotExist() {
        var actualResult = userService.createUserAndFolderIfNotExist(TEST_EMAIL, SignupMethod.GOOGLE);
        assertTrue(actualResult.isPresent());
        assertTrue(actualResult.get().getRole() == Role.USER);
        assertTrue(actualResult.get().getSignupMethod() == SignupMethod.GOOGLE);
    }

    @Test
    void notCreateOAuth2UserIfExist() {
        userService.createUserAndFolderIfNotExist(TEST_EMAIL, SignupMethod.GOOGLE);
        var actualResult = userService.createUserAndFolderIfNotExist(TEST_EMAIL, SignupMethod.GITGUB);
        assertFalse(actualResult.isPresent());
    }


}