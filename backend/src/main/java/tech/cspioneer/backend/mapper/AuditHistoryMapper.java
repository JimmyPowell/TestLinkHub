package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface AuditHistoryMapper {
    @Insert("""
    INSERT INTO meeting_audit_history (
        meeting_version_id, auditor_id, audit_status, comments, created_at
    ) VALUES (
        #{meetingVersionId}, #{auditorId}, #{auditStatus}, #{comments}, #{createdAt}
    )
""")
    void insertHistory(@Param("meetingVersionId") Long meetingVersionId,
                       @Param("auditorId") Long auditorId,
                       @Param("auditStatus") String auditStatus,
                       @Param("comments") String comments,
                       @Param("createdAt") LocalDateTime createdAt);

}
