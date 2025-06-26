package tech.cspioneer.backend.entity.dto.request;


import lombok.Data;

@Data
//管理员写的会议创建审核结果
public class MeetingReviewRequest {
    private String meeting_version_uuid;
    private String review_result;
    private String comments;

}
