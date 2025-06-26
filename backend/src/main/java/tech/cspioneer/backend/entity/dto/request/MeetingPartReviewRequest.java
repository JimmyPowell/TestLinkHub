package tech.cspioneer.backend.entity.dto.request;

import lombok.Data;

//创建会议审核结果的请求体
@Data
public class MeetingPartReviewRequest {
    private String partUuid;  //对应处理的会议
    private String reviewResult;  //前端输出的审核结果
    private String comments;  //前端输入的对审核结果的展示

}
