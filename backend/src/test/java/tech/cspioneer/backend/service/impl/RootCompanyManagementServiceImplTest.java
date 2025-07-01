package tech.cspioneer.backend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.cspioneer.backend.entity.Company;
import tech.cspioneer.backend.entity.dto.request.CompanyCreateRequest;
import tech.cspioneer.backend.entity.dto.request.CompanySearchRequest;
import tech.cspioneer.backend.entity.dto.request.CompanyUpdateRequest;
import tech.cspioneer.backend.entity.dto.response.CompanyResponse;
import tech.cspioneer.backend.exception.ResourceNotFoundException;
import tech.cspioneer.backend.exception.UserManagementException;
import tech.cspioneer.backend.mapper.RootCompanyManagementMapper;
import tech.cspioneer.backend.model.response.PagedResponse;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RootCompanyManagementServiceImplTest {
    @Mock RootCompanyManagementMapper companyMapper;
    @InjectMocks RootCompanyManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        reset(companyMapper);
    }

    @Test
    void testGetAllCompanies() {
        CompanySearchRequest req = new CompanySearchRequest();
        List<CompanyResponse> list = Collections.singletonList(new CompanyResponse());
        when(companyMapper.findWithFilters(eq(req), anyInt(), anyInt())).thenReturn(list);
        when(companyMapper.countWithFilters(eq(req))).thenReturn(10L);
        PagedResponse<CompanyResponse> resp = service.getAllCompanies(0, 10, req);
        assertEquals(1, resp.getContent().size());
        assertEquals(10, resp.getTotalElements());
        assertTrue(resp.isLast());
    }

    @Test
    void testGetCompanyByUuid_Success() {
        Company company = new Company();
        company.setUuid("uuid");
        company.setName("test");
        when(companyMapper.findByUuid("uuid")).thenReturn(company);
        CompanyResponse resp = service.getCompanyByUuid("uuid");
        assertEquals("test", resp.getName());
    }

    @Test
    void testGetCompanyByUuid_NotFound() {
        when(companyMapper.findByUuid("uuid")).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.getCompanyByUuid("uuid"));
    }

    @Test
    void testCreateCompany_Success() {
        CompanyCreateRequest req = new CompanyCreateRequest();
        req.setCompanyCode("code");
        when(companyMapper.findByCompanyCode("code")).thenReturn(null);
        when(companyMapper.insert(any())).thenReturn(1);
        CompanyResponse resp = service.createCompany(req);
        assertEquals("code", resp.getCompanyCode());
    }

    @Test
    void testCreateCompany_CodeExists() {
        CompanyCreateRequest req = new CompanyCreateRequest();
        req.setCompanyCode("code");
        when(companyMapper.findByCompanyCode("code")).thenReturn(new Company());
        assertThrows(UserManagementException.class, () -> service.createCompany(req));
    }

    @Test
    void testUpdateCompany_Success() {
        String uuid = "uuid";
        CompanyUpdateRequest req = new CompanyUpdateRequest();
        req.setCompanyCode("code");
        Company company = new Company();
        company.setUuid(uuid);
        company.setCompanyCode("code");
        when(companyMapper.findByUuid(uuid)).thenReturn(company);
        when(companyMapper.update(any())).thenReturn(1);
        CompanyResponse resp = service.updateCompany(uuid, req);
        assertNotNull(resp);
    }

    @Test
    void testUpdateCompany_NotFound() {
        String uuid = "uuid";
        CompanyUpdateRequest req = new CompanyUpdateRequest();
        when(companyMapper.findByUuid(uuid)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.updateCompany(uuid, req));
    }

    @Test
    void testUpdateCompany_CodeExists() {
        String uuid = "uuid";
        CompanyUpdateRequest req = new CompanyUpdateRequest();
        req.setCompanyCode("newcode");
        Company company = new Company();
        company.setUuid(uuid);
        company.setCompanyCode("oldcode");
        when(companyMapper.findByUuid(anyString())).thenReturn(company);
        assertThrows(ResourceNotFoundException.class, () -> service.updateCompany(uuid, req));
    }

    @Test
    void testDeleteCompany_Success() {
        String uuid = "uuid";
        Company company = new Company();
        company.setUuid(uuid);
        when(companyMapper.findByUuid(uuid)).thenReturn(company);
        when(companyMapper.softDelete(uuid)).thenReturn(1);
        service.deleteCompany(uuid);
        verify(companyMapper, times(1)).softDelete(uuid);
    }

    @Test
    void testDeleteCompany_NotFound() {
        String uuid = "uuid";
        when(companyMapper.findByUuid(uuid)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.deleteCompany(uuid));
    }

    @Test
    void testUpdateCompanyStatus_Success() {
        String uuid = "uuid";
        Company company = new Company();
        company.setUuid(uuid);
        when(companyMapper.findByUuid(uuid)).thenReturn(company);
        when(companyMapper.updateStatus(uuid, "ACTIVE")).thenReturn(1);
        when(companyMapper.findByUuid(uuid)).thenReturn(company);
        CompanyResponse resp = service.updateCompanyStatus(uuid, "ACTIVE");
        assertNotNull(resp);
    }

    @Test
    void testUpdateCompanyStatus_NotFound() {
        String uuid = "uuid";
        when(companyMapper.findByUuid(uuid)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.updateCompanyStatus(uuid, "ACTIVE"));
    }
} 