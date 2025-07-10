package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.LessonAuditHistory;
import java.util.List;

@Mapper
public interface LessonAuditHistoryMapper {

    @Insert("INSERT INTO lesson_audit_history(lesson_version_id, uuid, auditor_id, audit_status, comments, is_deleted, created_at) " +
            "VALUES(#{lessonVersionId}, #{uuid}, #{auditorId}, #{auditStatus}, #{comments}, #{isDeleted}, #{createdAt})")
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

    /**
     * 分页动态查询审核历史，支持审核状态、起止时间筛选
     * @param auditStatus 审核状态（可选）
     * @param beginTime 起始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageSize 每页数量
     * @param offset 偏移量
     * @return 审核历史列表
     */
    @Select({
        "<script>",
        "SELECT * FROM lesson_audit_history",
        "<where>",
        "  <if test='auditStatus != null and auditStatus != \"\"'>AND audit_status = #{auditStatus}</if>",
        "  <if test='beginTime != null and beginTime != \"\"'>AND created_at &gt;= #{beginTime}</if>",
        "  <if test='endTime != null and endTime != \"\"'>AND created_at &lt;= #{endTime}</if>",
        "  AND is_deleted = 0",
        "</where>",
        "ORDER BY created_at DESC",
        "LIMIT #{pageSize} OFFSET #{offset}",
        "</script>"
    })
    List<LessonAuditHistory> selectHistoryPage(@Param("auditStatus") String auditStatus,
                                               @Param("beginTime") String beginTime,
                                               @Param("endTime") String endTime,
                                               @Param("pageSize") int pageSize,
                                               @Param("offset") int offset);

    /**
     * 批量软删除审核历史（根据uuid）
     * @param uuids 审核历史uuid列表
     * @return 受影响行数
     */
    @Update({
        "<script>",
        "UPDATE lesson_audit_history SET is_deleted=1 WHERE uuid IN",
        "<foreach collection='uuids' item='uuid' open='(' separator=',' close=')'>",
        "  #{uuid}",
        "</foreach>",
        "</script>"
    })
    int softDeleteHistoryByUuids(@Param("uuids") List<String> uuids);

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

    /**
     * 查询审核历史总数，支持审核状态、起止时间筛选
     */
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM lesson_audit_history",
        "<where>",
        "  <if test='auditStatus != null and auditStatus != \"\"'>AND audit_status = #{auditStatus}</if>",
        "  <if test='beginTime != null and beginTime != \"\"'>AND created_at &gt;= #{beginTime}</if>",
        "  <if test='endTime != null and endTime != \"\"'>AND created_at &lt;= #{endTime}</if>",
        "  AND is_deleted = 0",
        "</where>",
        "</script>"
    })
    int countHistory(@Param("auditStatus") String auditStatus,
                    @Param("beginTime") String beginTime,
                    @Param("endTime") String endTime);
}
