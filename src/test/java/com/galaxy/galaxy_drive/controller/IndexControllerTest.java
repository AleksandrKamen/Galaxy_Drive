package com.galaxy.galaxy_drive.controller;

import com.galaxy.galaxy_drive.config.TestSecurityConfiguration;
import com.galaxy.galaxy_drive.model.dto.user.UserReadDto;
import com.galaxy.galaxy_drive.service.FileStorageService;
import com.galaxy.galaxy_drive.service.minio.MinioService;
import com.galaxy.galaxy_drive.service.user.UserService;
import com.galaxy.galaxy_drive.util.AuthenticationUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
@Import(TestSecurityConfiguration.class)
@WithMockUser
class IndexControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserService userService;
    @MockBean
    FileStorageService fileStorageService;
    @MockBean
    MessageSource messageSource;

    @Test
    void redirect_to_user_folder_ifPathEmpty() throws Exception {
        when(userService.findByUserName(any())).thenReturn(new UserReadDto(1L, "test@gmail.com", "", ""));
        mockMvc.perform(get("/").param("path", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?path=user-1-files"));
    }

    @Test
    void throwFolderNotFoundException_if_Folder_is_not_this_user() throws Exception {
        when(userService.findByUserName(any())).thenReturn(new UserReadDto(1L, "test@gmail.com", "", ""));
        mockMvc.perform(get("/").param("path", "user-2-files/test"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("errors_page/error404"));
    }

    @Test
    void throwFolderNotFoundException_if_Folder_is_not_Exist() throws Exception {
        when(userService.findByUserName(any())).thenReturn(new UserReadDto(1L, "test@gmail.com", "", ""));
        when(fileStorageService.isFolderExist(any())).thenReturn(false);
        mockMvc.perform(get("/").param("path", "user-1-files/test"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("errors_page/error404"));
    }

}