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
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.cspioneer.backend.entity.STSTemporaryCredentials;

import java.io.*;
import java.net.URL;
import java.util.Date;

@Component
public class OSSUtil {

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.region}")
    private String region;

    @Value("${oss.endpointSts}")
    private String endpointSts;

    @Value("${oss.accessKeyId}")
    private String accessKeyId;

    @Value("${oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${oss.accessKeyIdRam}")
    private String accessKeyIdRam;

    @Value("${oss.accessKeySecretRam}")
    private String accessKeySecretRam;

    @Value("${oss.bucketName}")
    private String bucketName;

    @Value("${oss.roleArn}")
    private String roleArn;

    @Value("${oss.roleSessionName}")
    private String roleSessionName;

    @Value("${oss.durationSeconds}")
    private Long durationSeconds;

    private OSSClientBuilder ossClientBuilder = new OSSClientBuilder();
    private DefaultAcsClient acsClient;

    public void setOssClientBuilder(OSSClientBuilder builder) {
        this.ossClientBuilder = builder;
    }

    public void setAcsClient(DefaultAcsClient acsClient) {
        this.acsClient = acsClient;
    }

    public STSTemporaryCredentials generateSTSCredentials() throws com.aliyuncs.exceptions.ClientException {
        DefaultProfile.addEndpoint(this.region, "Sts", endpointSts);
        IClientProfile profile = DefaultProfile.getProfile(this.region, accessKeyIdRam, accessKeySecretRam);

        DefaultAcsClient client = this.acsClient != null ? this.acsClient : new DefaultAcsClient(profile);
        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setSysMethod(MethodType.POST);
        request.setRoleArn(roleArn);
        request.setRoleSessionName(roleSessionName);
        request.setDurationSeconds(durationSeconds);
        AssumeRoleResponse response = client.getAcsResponse(request);

        return new STSTemporaryCredentials(
                response.getCredentials().getAccessKeyId(),
                response.getCredentials().getAccessKeySecret(),
                response.getCredentials().getSecurityToken(),
                response.getCredentials().getExpiration(),
                this.region,
                this.bucketName
        );
    }

    private OSS getOSSClient() {
        return ossClientBuilder.build(endpoint, accessKeyId, accessKeySecret);
    }

    public void createBucket(String bucketName) {
        OSS ossClient = getOSSClient();
        try {
            ossClient.createBucket(bucketName);
            System.out.println("Bucket created: " + bucketName);
        } catch (OSSException | ClientException e) {
            handleException(e);
        } finally {
            shutdownClient(ossClient);
        }
    }

    public void uploadFile(String bucketName, String objectName, String filePath) {
        OSS ossClient = getOSSClient();
        try (InputStream inputStream = new FileInputStream(new File(filePath))) {
            ossClient.putObject(bucketName, objectName, inputStream);
            System.out.println("File uploaded: " + objectName);
        } catch (OSSException | ClientException e) {
            handleException(e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shutdownClient(ossClient);
        }
    }

    public String generateDownloadUrl(String bucketName, String objectName, Date expiration) {
        OSS ossClient = getOSSClient();
        try {
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            return url.toString();
        } catch (OSSException | ClientException e) {
            handleException(e);
            return null;
        } finally {
            shutdownClient(ossClient);
        }
    }

    public void downloadFile(String bucketName, String objectName) {
        OSS ossClient = getOSSClient();
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, objectName);
            InputStream content = ossObject.getObjectContent();
            if (content != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                content.close();
            }
        } catch (OSSException | ClientException | IOException e) {
            handleException(e);
        } finally {
            shutdownClient(ossClient);
        }
    }

    public void listFiles(String bucketName) {
        OSS ossClient = getOSSClient();
        try {
            ObjectListing objectListing = ossClient.listObjects(bucketName);
            for (OSSObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                System.out.println(" - " + objectSummary.getKey() + " (size = " + objectSummary.getSize() + ")");
            }
        } catch (OSSException | ClientException e) {
            handleException(e);
        } finally {
            shutdownClient(ossClient);
        }
    }

    public void deleteFile(String bucketName, String objectName) {
        OSS ossClient = getOSSClient();
        try {
            ossClient.deleteObject(bucketName, objectName);
            System.out.println("File deleted: " + objectName);
        } catch (OSSException | ClientException e) {
            handleException(e);
        } finally {
            shutdownClient(ossClient);
        }
    }

    public String getBucketName() {
        return this.bucketName;
    }

    private void handleException(Exception e) {
        if (e instanceof OSSException) {
            OSSException oe = (OSSException) e;
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message: " + oe.getErrorMessage());
            System.out.println("Error Code: " + oe.getErrorCode());
            System.out.println("Request ID: " + oe.getRequestId());
            System.out.println("Host ID: " + oe.getHostId());
        } else if (e instanceof ClientException) {
            ClientException ce = (ClientException) e;
            System.out.println("Caught a ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ce.getMessage());
        }
    }

    private void shutdownClient(OSS ossClient) {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

}
