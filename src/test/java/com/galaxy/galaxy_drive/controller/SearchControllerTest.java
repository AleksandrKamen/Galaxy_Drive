package com.galaxy.galaxy_drive.controller;

import com.galaxy.galaxy_drive.config.TestSecurityConfiguration;
import com.galaxy.galaxy_drive.model.dto.user.UserReadDto;
import com.galaxy.galaxy_drive.service.minio.MinioService;
import com.galaxy.galaxy_drive.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    MinioService minioService;
  @Test
  void search_Page_Rendered_Successfully() throws Exception {
      when(userService.findByUserName(any())).thenReturn(new UserReadDto(1L, "test@gmail.com", "", ""));
      when(minioService.searchFolderByName(any(), any())).thenReturn(anyList());
      mockMvc.perform(get("/search").param("query", "test"))
              .andExpect(status().is2xxSuccessful())
              .andExpect(view().name("search"))
              .andExpect(model().attributeExists("user", "foundFolders", "foundFiles", "query", "userFolderName"));
  }

}