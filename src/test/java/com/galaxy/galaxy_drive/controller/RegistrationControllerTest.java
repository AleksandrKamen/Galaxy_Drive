package com.galaxy.galaxy_drive.controller;

import com.galaxy.galaxy_drive.config.TestSecurityConfiguration;
import com.galaxy.galaxy_drive.exception.user.UserAlreadyExistsException;
import com.galaxy.galaxy_drive.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrationController.class)
@Import(TestSecurityConfiguration.class)
class RegistrationControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserService userService;
    static final String USER_EMAIL = "test@gmail.com";
    static final String USER_PASSWORD = "123";

    @Test
    void registration_Page_Rendered_Successfully() throws Exception {
        mockMvc.perform(get("/registration"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("registration"));
    }

    @Test
    void registration_Successful() throws Exception {
        when(userService.create(any())).thenReturn(any());
        mockMvc.perform(post("/registration")
                        .param("userName", USER_EMAIL)
                        .param("rawPassword", USER_PASSWORD)
                        .param("confirmPassword", USER_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));
    }

    @Test
    void registration_IfUserAlreadyExist() throws Exception {
        when(userService.create(any())).thenThrow(new UserAlreadyExistsException("user already exist"));
        mockMvc.perform(post("/registration")
                        .param("userName", USER_EMAIL)
                        .param("rawPassword", USER_PASSWORD)
                        .param("confirmPassword", USER_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("registration"));
    }
    @Test
    void registration_Password_and_confirmPassword_notEquals() throws Exception {
        mockMvc.perform(post("/registration")
                        .param("userName", USER_EMAIL)
                        .param("rawPassword", USER_PASSWORD)
                        .param("confirmPassword", "1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("registration"))
                .andExpect(flash().attributeExists("errors"));
        verify(userService, never()).create(any());
    }
    @Test
    void registration_shortPassword() throws Exception {
        mockMvc.perform(post("/registration")
                        .param("userName", USER_EMAIL)
                        .param("rawPassword", "12")
                        .param("confirmPassword", "12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("registration"))
                .andExpect(flash().attributeExists("errors"));
        verify(userService, never()).create(any());
    }

    @Test
    void registration_longPassword() throws Exception {
        mockMvc.perform(post("/registration")
                        .param("userName", USER_EMAIL)
                        .param("rawPassword", "12345678910")
                        .param("confirmPassword", "12345678910"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("registration"))
                .andExpect(flash().attributeExists("errors"));
        verify(userService, never()).create(any());
    }

    @Test
    void registration_InvalidEmail() throws Exception {
        mockMvc.perform(post("/registration")
                        .param("userName", "email")
                        .param("rawPassword", USER_PASSWORD)
                        .param("confirmPassword", USER_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("registration"))
                .andExpect(flash().attributeExists("errors"));
        verify(userService, never()).create(any());
    }



}