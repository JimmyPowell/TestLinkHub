package tech.cspioneer.backend.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.auth.sts.AssumeRoleResponse.Credentials;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tech.cspioneer.backend.entity.STSTemporaryCredentials;

import java.io.*;
import java.net.URL;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OSSUtilTest {
    private OSSUtil ossUtil;

    @BeforeEach
    void setUp() {
        ossUtil = new OSSUtil();
        setField(ossUtil, "endpoint", "endpoint");
        setField(ossUtil, "region", "region");
        setField(ossUtil, "endpointSts", "endpointSts");
        setField(ossUtil, "accessKeyId", "ak");
        setField(ossUtil, "accessKeySecret", "sk");
        setField(ossUtil, "accessKeyIdRam", "akram");
        setField(ossUtil, "accessKeySecretRam", "skram");
        setField(ossUtil, "bucketName", "bucket");
        setField(ossUtil, "roleArn", "rolearn");
        setField(ossUtil, "roleSessionName", "session");
        setField(ossUtil, "durationSeconds", 1000L);
    }

    private void setField(Object obj, String field, Object value) {
        try {
            java.lang.reflect.Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetBucketName() {
        assertEquals("bucket", ossUtil.getBucketName());
    }

    @Test
    void testCreateBucket_NormalAndException() {
        OSS mockOss = mock(OSS.class);
        OSSClientBuilder mockBuilder = mock(OSSClientBuilder.class);
        when(mockBuilder.build(anyString(), anyString(), anyString())).thenReturn(mockOss);
        ossUtil.setOssClientBuilder(mockBuilder);
        // 正常分支
        ossUtil.createBucket("bucket");
        verify(mockOss).createBucket("bucket");
        verify(mockOss).shutdown();
        // 异常分支
        doThrow(new OSSException("err")).when(mockOss).createBucket("bucket2");
        ossUtil.createBucket("bucket2");
    }

    @Test
    void testUploadFile_NormalAndException() throws Exception {
        OSS mockOss = mock(OSS.class);
        OSSClientBuilder mockBuilder = mock(OSSClientBuilder.class);
        when(mockBuilder.build(anyString(), anyString(), anyString())).thenReturn(mockOss);
        ossUtil.setOssClientBuilder(mockBuilder);
        // 创建临时文件
        File temp = File.createTempFile("ossutiltest", ".txt");
        temp.deleteOnExit();
        try (FileWriter fw = new FileWriter(temp)) { fw.write("abc"); }
        ossUtil.uploadFile("bucket", "obj", temp.getAbsolutePath());
        verify(mockOss).putObject(eq("bucket"), eq("obj"), any(InputStream.class));
        verify(mockOss).shutdown();
        // OSSException分支
        doThrow(new OSSException("err")).when(mockOss).putObject(anyString(), anyString(), any(InputStream.class));
        ossUtil.uploadFile("bucket", "obj2", temp.getAbsolutePath());
    }

    @Test
    void testGenerateDownloadUrl_NormalAndException() throws Exception {
        OSS mockOss = mock(OSS.class);
        OSSClientBuilder mockBuilder = mock(OSSClientBuilder.class);
        when(mockBuilder.build(anyString(), anyString(), anyString())).thenReturn(mockOss);
        ossUtil.setOssClientBuilder(mockBuilder);
        when(mockOss.generatePresignedUrl(anyString(), anyString(), any(Date.class))).thenReturn(new URL("http://test.com/abc"));
        String url = ossUtil.generateDownloadUrl("bucket", "obj", new Date());
        assertEquals("http://test.com/abc", url);
        // 异常分支
        doThrow(new OSSException("err")).when(mockOss).generatePresignedUrl(anyString(), anyString(), any(Date.class));
        assertNull(ossUtil.generateDownloadUrl("bucket", "obj", new Date()));
    }

    @Test
    void testDownloadFile_NormalAndException() throws Exception {
        OSS mockOss = mock(OSS.class);
        OSSClientBuilder mockBuilder = mock(OSSClientBuilder.class);
        when(mockBuilder.build(anyString(), anyString(), anyString())).thenReturn(mockOss);
        ossUtil.setOssClientBuilder(mockBuilder);
        OSSObject mockObj = mock(OSSObject.class);
        when(mockOss.getObject(anyString(), anyString())).thenReturn(mockObj);
        when(mockObj.getObjectContent()).thenReturn(new ByteArrayInputStream("hello\nworld".getBytes()));
        ossUtil.downloadFile("bucket", "obj");
        verify(mockOss).shutdown();
        // 异常分支
        doThrow(new OSSException("err")).when(mockOss).getObject(anyString(), anyString());
        ossUtil.downloadFile("bucket", "obj2");
    }

    @Test
    void testListFiles_NormalAndException() {
        OSS mockOss = mock(OSS.class);
        OSSClientBuilder mockBuilder = mock(OSSClientBuilder.class);
        when(mockBuilder.build(anyString(), anyString(), anyString())).thenReturn(mockOss);
        ossUtil.setOssClientBuilder(mockBuilder);
        ObjectListing listing = mock(ObjectListing.class);
        OSSObjectSummary summary = mock(OSSObjectSummary.class);
        when(summary.getKey()).thenReturn("file1");
        when(summary.getSize()).thenReturn(123L);
        when(listing.getObjectSummaries()).thenReturn(Collections.singletonList(summary));
        when(mockOss.listObjects(anyString())).thenReturn(listing);
        ossUtil.listFiles("bucket");
        // 异常分支
        doThrow(new OSSException("err")).when(mockOss).listObjects(anyString());
        ossUtil.listFiles("bucket2");
    }

    @Test
    void testDeleteFile_NormalAndException() {
        OSS mockOss = mock(OSS.class);
        OSSClientBuilder mockBuilder = mock(OSSClientBuilder.class);
        when(mockBuilder.build(anyString(), anyString(), anyString())).thenReturn(mockOss);
        ossUtil.setOssClientBuilder(mockBuilder);
        ossUtil.deleteFile("bucket", "obj");
        verify(mockOss).deleteObject("bucket", "obj");
        // 异常分支
        doThrow(new OSSException("err")).when(mockOss).deleteObject(anyString(), anyString());
        ossUtil.deleteFile("bucket", "obj2");
    }

    // generateSTSCredentials 依赖阿里云 STS SDK，通常集成测试或 mock
    // 这里只做简单 mock 验证
    @Test
    void testGenerateSTSCredentials() throws Exception {
        try (MockedStatic<DefaultProfile> profileMockedStatic = Mockito.mockStatic(DefaultProfile.class)) {
            DefaultProfile profile = mock(DefaultProfile.class);
            profileMockedStatic.when(() -> DefaultProfile.getProfile(anyString(), anyString(), anyString())).thenReturn(profile);

            // 直接 mock DefaultAcsClient
            DefaultAcsClient acsClient = mock(DefaultAcsClient.class);

            AssumeRoleResponse response = mock(AssumeRoleResponse.class);
            AssumeRoleResponse.Credentials creds = Mockito.spy(new AssumeRoleResponse.Credentials());
            doReturn("ak").when(creds).getAccessKeyId();
            doReturn("sk").when(creds).getAccessKeySecret();
            doReturn("token").when(creds).getSecurityToken();
            doReturn("exp").when(creds).getExpiration();
            when(response.getCredentials()).thenReturn(creds);

            when(acsClient.getAcsResponse(any(AssumeRoleRequest.class))).thenReturn(response);

            // 反射注入 acsClient 到 ossUtil（如果有该字段）
            try {
                java.lang.reflect.Field acsClientField = ossUtil.getClass().getDeclaredField("acsClient");
                acsClientField.setAccessible(true);
                acsClientField.set(ossUtil, acsClient);
            } catch (NoSuchFieldException e) {
                // 如果没有该字段，说明 ossUtil 里是 new 的，只能考虑重构 OSSUtil 支持注入
            }

            STSTemporaryCredentials result = ossUtil.generateSTSCredentials();
            assertEquals("ak", result.getAccessKeyId());
            assertEquals("sk", result.getAccessKeySecret());
            assertEquals("token", result.getSecurityToken());
            assertEquals("exp", result.getExpiration());
        }
    }

    @Test
    void testSettersAndGetter() {
        OSSClientBuilder builder = new OSSClientBuilder();
        ossUtil.setOssClientBuilder(builder);
        DefaultAcsClient acsClient = mock(DefaultAcsClient.class);
        ossUtil.setAcsClient(acsClient);
        assertEquals("bucket", ossUtil.getBucketName());
    }

    @Test
    void testGenerateSTSCredentials_ClientException() throws Exception {
        try (MockedStatic<DefaultProfile> profileMockedStatic = Mockito.mockStatic(DefaultProfile.class)) {
            DefaultProfile profile = mock(DefaultProfile.class);
            profileMockedStatic.when(() -> DefaultProfile.getProfile(anyString(), anyString(), anyString())).thenReturn(profile);
            DefaultAcsClient acsClient = mock(DefaultAcsClient.class);
            when(acsClient.getAcsResponse(any(AssumeRoleRequest.class))).thenThrow(new com.aliyuncs.exceptions.ClientException("err"));
            ossUtil.setAcsClient(acsClient);
            assertThrows(com.aliyuncs.exceptions.ClientException.class, () -> ossUtil.generateSTSCredentials());
        }
    }

    @Test
    void testCreateBucket_ClientException() {
        OSS mockOss = mock(OSS.class);
        OSSClientBuilder mockBuilder = mock(OSSClientBuilder.class);
        when(mockBuilder.build(anyString(), anyString(), anyString())).thenReturn(mockOss);
        ossUtil.setOssClientBuilder(mockBuilder);
        doThrow(new ClientException("err")).when(mockOss).createBucket("bucket3");
        ossUtil.createBucket("bucket3");
    }

    @Test
    void testUploadFile_ClientException() throws Exception {
        OSS mockOss = mock(OSS.class);
        OSSClientBuilder mockBuilder = mock(OSSClientBuilder.class);
        when(mockBuilder.build(anyString(), anyString(), anyString())).thenReturn(mockOss);
        ossUtil.setOssClientBuilder(mockBuilder);
        File temp = File.createTempFile("ossutiltest", ".txt");
        temp.deleteOnExit();
        try (FileWriter fw = new FileWriter(temp)) { fw.write("abc"); }
        doThrow(new ClientException("err")).when(mockOss).putObject(anyString(), anyString(), any(InputStream.class));
        ossUtil.uploadFile("bucket", "obj3", temp.getAbsolutePath());
    }

    @Test
    void testGenerateDownloadUrl_ClientException() {
        OSS mockOss = mock(OSS.class);
        OSSClientBuilder mockBuilder = mock(OSSClientBuilder.class);
        when(mockBuilder.build(anyString(), anyString(), anyString())).thenReturn(mockOss);
        ossUtil.setOssClientBuilder(mockBuilder);
        doThrow(new ClientException("err")).when(mockOss).generatePresignedUrl(anyString(), anyString(), any(Date.class));
        assertNull(ossUtil.generateDownloadUrl("bucket", "obj3", new Date()));
    }

    @Test
    void testDownloadFile_ClientException() {
        OSS mockOss = mock(OSS.class);
        OSSClientBuilder mockBuilder = mock(OSSClientBuilder.class);
        when(mockBuilder.build(anyString(), anyString(), anyString())).thenReturn(mockOss);
        ossUtil.setOssClientBuilder(mockBuilder);
        doThrow(new ClientException("err")).when(mockOss).getObject(anyString(), anyString());
        ossUtil.downloadFile("bucket", "obj3");
    }

    @Test
    void testListFiles_ClientException() {
        OSS mockOss = mock(OSS.class);
        OSSClientBuilder mockBuilder = mock(OSSClientBuilder.class);
        when(mockBuilder.build(anyString(), anyString(), anyString())).thenReturn(mockOss);
        ossUtil.setOssClientBuilder(mockBuilder);
        doThrow(new ClientException("err")).when(mockOss).listObjects(anyString());
        ossUtil.listFiles("bucket3");
    }

    @Test
    void testDeleteFile_ClientException() {
        OSS mockOss = mock(OSS.class);
        OSSClientBuilder mockBuilder = mock(OSSClientBuilder.class);
        when(mockBuilder.build(anyString(), anyString(), anyString())).thenReturn(mockOss);
        ossUtil.setOssClientBuilder(mockBuilder);
        doThrow(new ClientException("err")).when(mockOss).deleteObject(anyString(), anyString());
        ossUtil.deleteFile("bucket", "obj3");
    }
} 