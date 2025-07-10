package tech.cspioneer.backend.service;

import com.aliyuncs.exceptions.ClientException;
import tech.cspioneer.backend.entity.STSTemporaryCredentials;

public interface OSSService {
    STSTemporaryCredentials getSTSCredentials() throws ClientException;
}
