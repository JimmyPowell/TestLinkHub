package tech.cspioneer.backend.entity.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//管理员写的会议创建审核结果
public class MeetingReviewRequest {
    private String meetingVersionUuid;
    private String auditStatus;
    private String comments;

}
