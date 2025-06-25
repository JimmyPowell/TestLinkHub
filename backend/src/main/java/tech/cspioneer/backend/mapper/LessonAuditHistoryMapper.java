package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.LessonAuditHistory;
import java.util.List;

@Mapper
public interface LessonAuditHistoryMapper {

    @Insert("INSERT INTO lesson_audit_history(lesson_version_id, auditor_id, audit_status, comments, is_deleted, created_at) " +
            "VALUES(#{lessonVersionId}, #{auditorId}, #{auditStatus}, #{comments}, #{isDeleted}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LessonAuditHistory lessonAuditHistory);

    @Update("UPDATE lesson_audit_history SET lesson_version_id=#{lessonVersionId}, auditor_id=#{auditorId}, audit_status=#{auditStatus}, comments=#{comments}, is_deleted=#{isDeleted} WHERE id=#{id}")
    int update(LessonAuditHistory lessonAuditHistory);

    @Delete("DELETE FROM lesson_audit_history WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT * FROM lesson_audit_history WHERE id=#{id}")
    LessonAuditHistory selectById(@Param("id") Long id);

    @Select("SELECT * FROM lesson_audit_history WHERE lesson_version_id=#{lessonVersionId}")
    List<LessonAuditHistory> selectByLessonVersionId(@Param("lessonVersionId") Long lessonVersionId);

    @Select("SELECT * FROM lesson_audit_history")
    List<LessonAuditHistory> selectAll();

    // 分页动态查询
    @Select({
        "<script>",
        "SELECT * FROM lesson_audit_history",
        "<where>",
        "  <if test='auditStatus != null'>AND audit_status = #{auditStatus}</if>",
        "  <if test='auditorId != null'>AND auditor_id = #{auditorId}</if>",
        "  <if test='lessonVersionId != null'>AND lesson_version_id = #{lessonVersionId}</if>",
        "  <if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>",
        "</where>",
        "ORDER BY created_at DESC",
        "LIMIT #{pageSize} OFFSET #{offset}",
        "</script>"
    })
    List<LessonAuditHistory> selectPage(@Param("auditStatus") String auditStatus,
                                        @Param("auditorId") Long auditorId,
                                        @Param("lessonVersionId") Long lessonVersionId,
                                        @Param("isDeleted") Boolean isDeleted,
                                        @Param("pageSize") int pageSize,
                                        @Param("offset") int offset);

    @Select({
        "<script>",
        "SELECT COUNT(*) FROM lesson_audit_history",
        "<where>",
        "  <if test='auditStatus != null'>AND audit_status = #{auditStatus}</if>",
        "  <if test='auditorId != null'>AND auditor_id = #{auditorId}</if>",
        "  <if test='lessonVersionId != null'>AND lesson_version_id = #{lessonVersionId}</if>",
        "  <if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>",
        "</where>",
        "</script>"
    })
    int countPage(@Param("auditStatus") String auditStatus,
                  @Param("auditorId") Long auditorId,
                  @Param("lessonVersionId") Long lessonVersionId,
                  @Param("isDeleted") Boolean isDeleted);
}
