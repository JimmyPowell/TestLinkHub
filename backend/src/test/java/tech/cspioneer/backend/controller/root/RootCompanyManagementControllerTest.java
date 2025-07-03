package tech.cspioneer.backend.controller.root;

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
import tech.cspioneer.backend.entity.dto.request.CompanyCreateRequest;
import tech.cspioneer.backend.entity.dto.request.CompanySearchRequest;
import tech.cspioneer.backend.entity.dto.request.CompanyUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.CompanyResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.exception.UserManagementException;
import tech.cspioneer.backend.model.response.PagedResponse;
import tech.cspioneer.backend.service.RootCompanyManagementService;
import tech.cspioneer.backend.utils.JwtUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RootCompanyManagementController.class)
public class RootCompanyManagementControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private RootCompanyManagementService companyService;
    @MockBean
    private JwtUtils jwtUtils;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable());
            return http.build();
        }
    }

    @Test
    @DisplayName("获取公司列表-正常")
    void testGetAllCompanies_Success() throws Exception {
        when(companyService.getAllCompanies(anyInt(), anyInt(), any())).thenReturn(new PagedResponse<>(Collections.emptyList(), 0, 1, 0, 0, false));
        mockMvc.perform(get("/api/root/companies?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取公司详情-正常")
    void testGetCompanyByUuid_Success() throws Exception {
        when(companyService.getCompanyByUuid(anyString())).thenReturn(new CompanyResponse());
        mockMvc.perform(get("/api/root/companies/uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取公司详情-未找到")
    void testGetCompanyByUuid_NotFound() throws Exception {
        when(companyService.getCompanyByUuid(anyString())).thenThrow(new ResourceNotFoundException("not found"));
        mockMvc.perform(get("/api/root/companies/uuid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("创建公司-正常")
    void testCreateCompany_Success() throws Exception {
        CompanyCreateRequest req = new CompanyCreateRequest();
        req.setName("test");
        req.setCompanyCode("code");
        when(companyService.createCompany(any())).thenReturn(new CompanyResponse());
        mockMvc.perform(post("/api/root/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201));
    }

    @Test
    @DisplayName("创建公司-业务异常")
    void testCreateCompany_BadRequest() throws Exception {
        CompanyCreateRequest req = new CompanyCreateRequest();
        req.setName("test");
        req.setCompanyCode("code");
        when(companyService.createCompany(any())).thenThrow(new UserManagementException("error"));
        mockMvc.perform(post("/api/root/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("创建公司-参数校验异常")
    void testCreateCompany_ValidationError() throws Exception {
        CompanyCreateRequest req = new CompanyCreateRequest();
        mockMvc.perform(post("/api/root/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("更新公司-正常")
    void testUpdateCompany_Success() throws Exception {
        CompanyUpdateRequest req = new CompanyUpdateRequest();
        when(companyService.updateCompany(anyString(), any())).thenReturn(new CompanyResponse());
        mockMvc.perform(put("/api/root/companies/uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("更新公司-未找到")
    void testUpdateCompany_NotFound() throws Exception {
        CompanyUpdateRequest req = new CompanyUpdateRequest();
        when(companyService.updateCompany(anyString(), any())).thenThrow(new ResourceNotFoundException("not found"));
        mockMvc.perform(put("/api/root/companies/uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("更新公司-业务异常")
    void testUpdateCompany_BadRequest() throws Exception {
        CompanyUpdateRequest req = new CompanyUpdateRequest();
        when(companyService.updateCompany(anyString(), any())).thenThrow(new UserManagementException("error"));
        mockMvc.perform(put("/api/root/companies/uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("删除公司-正常")
    void testDeleteCompany_Success() throws Exception {
        mockMvc.perform(delete("/api/root/companies/uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(204));
    }

    @Test
    @DisplayName("删除公司-未找到")
    void testDeleteCompany_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("not found")).when(companyService).deleteCompany(anyString());
        mockMvc.perform(delete("/api/root/companies/uuid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("更新公司状态-正常")
    void testUpdateCompanyStatus_Success() throws Exception {
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("status", "ACTIVE");
        when(companyService.updateCompanyStatus(anyString(), anyString())).thenReturn(new CompanyResponse());
        mockMvc.perform(put("/api/root/companies/uuid/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("更新公司状态-未找到")
    void testUpdateCompanyStatus_NotFound() throws Exception {
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("status", "ACTIVE");
        when(companyService.updateCompanyStatus(anyString(), anyString())).thenThrow(new ResourceNotFoundException("not found"));
        mockMvc.perform(put("/api/root/companies/uuid/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusMap)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("更新公司状态-参数为空")
    void testUpdateCompanyStatus_EmptyStatus() throws Exception {
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("status", "");
        mockMvc.perform(put("/api/root/companies/uuid/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusMap)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("更新公司状态-参数非法")
    void testUpdateCompanyStatus_IllegalArgument() throws Exception {
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("status", "INVALID");
        when(companyService.updateCompanyStatus(anyString(), anyString())).thenThrow(new IllegalArgumentException("Invalid status value."));
        mockMvc.perform(put("/api/root/companies/uuid/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusMap)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
} 