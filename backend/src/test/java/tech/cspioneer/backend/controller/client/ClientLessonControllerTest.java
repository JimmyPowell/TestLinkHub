package tech.cspioneer.backend.controller.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tech.cspioneer.backend.entity.dto.response.LessonListResponse;
import tech.cspioneer.backend.entity.dto.response.LessonDetailResponse;
import tech.cspioneer.backend.entity.dto.response.LessonSearchResponse;
import tech.cspioneer.backend.service.LessonService;
import tech.cspioneer.backend.utils.JwtUtils;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientLessonController.class)
public class ClientLessonControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private LessonService lessonService;
    @MockBean
    private JwtUtils jwtUtils;
    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable());
            return http.build();
        }
    }

    private void setAuth(String role) {
        Authentication auth = new UsernamePasswordAuthenticationToken("mock-uuid", null, Collections.singleton(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("getAllLessons-正常")
    void testGetAllLessons_Success() throws Exception {
        setAuth("USER");
        when(lessonService.getAllLessons(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(new LessonListResponse());
        mockMvc.perform(get("/api/user/lesson/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("getAllLessons-无权限")
    void testGetAllLessons_Forbidden() throws Exception {
        setAuth("GUEST");
        mockMvc.perform(get("/api/user/lesson/all"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("getAllLessons-服务异常")
    void testGetAllLessons_Exception() throws Exception {
        setAuth("USER");
        when(lessonService.getAllLessons(any(), any(), any(), any(), anyInt(), anyInt())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/user/lesson/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("getLessonDetail-正常")
    void testGetLessonDetail_Success() throws Exception {
        setAuth("COMPANY");
        when(lessonService.getLessonDetail(anyString(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(new LessonDetailResponse());
        mockMvc.perform(get("/api/user/lesson/detail")
                .param("uuid", "lesson-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("getLessonDetail-无权限")
    void testGetLessonDetail_Forbidden() throws Exception {
        setAuth("GUEST");
        mockMvc.perform(get("/api/user/lesson/detail")
                .param("uuid", "lesson-uuid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("getLessonDetail-服务异常")
    void testGetLessonDetail_Exception() throws Exception {
        setAuth("ADMIN");
        when(lessonService.getLessonDetail(anyString(), anyInt(), anyInt(), anyString(), anyString())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/user/lesson/detail")
                .param("uuid", "lesson-uuid"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }

    @Test
    @DisplayName("searchLesson-正常")
    void testSearchLesson_Success() throws Exception {
        setAuth("USER");
        when(lessonService.searchLesson(any())).thenReturn(new LessonSearchResponse());
        mockMvc.perform(get("/api/user/lesson/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("searchLesson-无权限")
    void testSearchLesson_Forbidden() throws Exception {
        setAuth("GUEST");
        mockMvc.perform(get("/api/user/lesson/search"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("searchLesson-服务异常")
    void testSearchLesson_Exception() throws Exception {
        setAuth("COMPANY");
        when(lessonService.searchLesson(any())).thenThrow(new RuntimeException("error"));
        mockMvc.perform(get("/api/user/lesson/search"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(5000));
    }
} 