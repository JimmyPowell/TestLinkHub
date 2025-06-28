package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;
import java.util.List;

/**
 * 课程上传请求体
 */
@Data
public class LessonUploadRequest {
    /**
     * 课程名称
     */
    private String name;
    
    /**
     * 课程图片URL
     */
    private String imageUrl;
    
    /**
     * 课程描述
     */
    private String description;
    
    /**
     * 作者姓名
     */
    private String authorName;
    
    /**
     * 资源类型
     */
    private String resourcesType;
    
    /**
     * 资源URL列表
     */
    private List<String> resourcesUrls;
    
    /**
     * 排序顺序列表
     */
    private List<Integer> sortOrders;
} 