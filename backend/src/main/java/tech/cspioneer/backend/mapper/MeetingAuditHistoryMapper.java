package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.cspioneer.backend.entity.MeetingAuditHistory;

@Mapper
public interface MeetingAuditHistoryMapper {

    @Insert("""
        INSERT INTO meeting_audit_history (meeting_version_id, auditor_id, audit_status, comments, created_at, is_deleted)
        VALUES (#{history.meetingVersionId}, #{history.auditorId}, #{history.auditStatus}, #{history.comments}, #{history.createdAt}, 0)
    """)
    void insert(@Param("history") MeetingAuditHistory history);
}
