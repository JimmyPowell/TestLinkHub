package tech.cspioneer.backend.entity.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码验证请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCoderequest {
    
    /**
     * 验证码
     */
    private String code;
    
    /**
     * 邮箱地址
     */
    private String email;
}
