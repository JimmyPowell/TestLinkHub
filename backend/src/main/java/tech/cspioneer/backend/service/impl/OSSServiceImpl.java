package tech.cspioneer.backend.service.impl;

import com.aliyuncs.exceptions.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.cspioneer.backend.entity.STSTemporaryCredentials;
import tech.cspioneer.backend.service.OSSService;
import tech.cspioneer.backend.utils.OSSUtil;

@Service
@RequiredArgsConstructor
public class OSSServiceImpl implements OSSService {

    private final OSSUtil ossUtil;

    @Override
    public STSTemporaryCredentials getSTSCredentials() throws ClientException {
        return ossUtil.generateSTSCredentials();
    }
}
