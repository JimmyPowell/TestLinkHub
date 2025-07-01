package tech.cspioneer.backend.service.impl;

import com.aliyuncs.exceptions.ClientException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.cspioneer.backend.entity.STSTemporaryCredentials;
import tech.cspioneer.backend.utils.OSSUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OSSServiceImplTest {
    @Mock OSSUtil ossUtil;
    @InjectMocks OSSServiceImpl ossService;

    @Test
    void testGetSTSCredentials_Success() throws ClientException {
        STSTemporaryCredentials credentials = new STSTemporaryCredentials();
        when(ossUtil.generateSTSCredentials()).thenReturn(credentials);
        STSTemporaryCredentials result = ossService.getSTSCredentials();
        assertSame(credentials, result);
        verify(ossUtil, times(1)).generateSTSCredentials();
    }

    @Test
    void testGetSTSCredentials_ClientException() throws ClientException {
        when(ossUtil.generateSTSCredentials()).thenThrow(new ClientException("err"));
        assertThrows(ClientException.class, () -> ossService.getSTSCredentials());
        verify(ossUtil, times(1)).generateSTSCredentials();
    }
} 