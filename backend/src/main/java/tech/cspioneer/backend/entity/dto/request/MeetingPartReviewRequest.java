package tech.cspioneer.backend.entity.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//创建会议审核结果的请求体
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingPartReviewRequest {
    private String partUuid;  //对应处理的会议
    private String reviewResult;  //前端输出的审核结果
    private String comments;  //前端输入的对审核结果的展示

}
