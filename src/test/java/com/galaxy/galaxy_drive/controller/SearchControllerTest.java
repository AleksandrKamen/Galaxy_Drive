package com.galaxy.galaxy_drive.controller;

import com.galaxy.galaxy_drive.config.TestSecurityConfiguration;
import com.galaxy.galaxy_drive.model.dto.user.UserReadDto;
import com.galaxy.galaxy_drive.service.FileStorageService;
import com.galaxy.galaxy_drive.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
@Import(TestSecurityConfiguration.class)
@WithMockUser
class SearchControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserService userService;
    @MockBean
    FileStorageService fileStorageService;
    @Autowired
    WebApplicationContext wac;


    @Test
    void search_Page_Rendered_Successfully() throws Exception {
        var session = new MockHttpSession();
        session.setAttribute("user", new UserReadDto(1L, "test@gmail.com", "", ""));
        session.setAttribute("userFolder", "user-1-files");
        when(fileStorageService.searchFolderByName("user-1-files", "test")).thenReturn(Collections.emptyList());
        when(fileStorageService.searchFileByName("user-1-files", "test")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/search").param("query", "test").session(session))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("search"))
                .andExpect(model().attributeExists("foundFolders", "foundFiles"));
    }

}