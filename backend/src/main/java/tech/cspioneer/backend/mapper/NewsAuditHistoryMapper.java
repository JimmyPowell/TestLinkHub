package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.NewsAuditHistory;

import java.util.List;

@Mapper
public interface NewsAuditHistoryMapper {

    /**
     * 插入审核记录
     * @param history 审核历史对象
     * @return 插入的行数
     */
    @Insert("INSERT INTO news_audit_history(news_content_id, auditor_id, audit_status, comments) " +
            "VALUES(#{newsContentId}, #{auditorId}, #{auditStatus}, #{comments})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(NewsAuditHistory history);

    /**
     * 根据主键ID查询审核记录
     * @param id 主键ID
     * @return 审核历史对象
     */
    @Select("SELECT id, news_content_id, auditor_id, audit_status, comments, is_deleted, created_at " +
            "FROM news_audit_history " +
            "WHERE id = #{id} AND is_deleted = 0")
    @Results(id = "auditHistoryMap", value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "news_content_id", property = "newsContentId"),
            @Result(column = "auditor_id", property = "auditorId"),
            @Result(column = "audit_status", property = "auditStatus"),
            @Result(column = "comments", property = "comments"),
            @Result(column = "is_deleted", property = "isDeleted"),
            @Result(column = "created_at", property = "createdAt")
    })
    NewsAuditHistory selectById(@Param("id") Long id);

    /**
     * 根据新闻内容ID查询审核记录（按时间倒序）
     * @param newsContentId 新闻内容ID
     * @return 审核历史列表
     */
    @Select("SELECT id, news_content_id, auditor_id, audit_status, comments, is_deleted, created_at " +
            "FROM news_audit_history " +
            "WHERE news_content_id = #{newsContentId} AND is_deleted = 0 " +
            "ORDER BY created_at DESC")
    @ResultMap("auditHistoryMap")
    List<NewsAuditHistory> selectByNewsContentId(@Param("newsContentId") Long newsContentId);

    /**
     * 根据审核员ID查询审核记录（按时间倒序）
     * @param auditorId 审核员ID
     * @return 审核历史列表
     */
    @Select("SELECT id, news_content_id, auditor_id, audit_status, comments, is_deleted, created_at " +
            "FROM news_audit_history " +
            "WHERE auditor_id = #{auditorId} AND is_deleted = 0 " +
            "ORDER BY created_at DESC")
    @ResultMap("auditHistoryMap")
    List<NewsAuditHistory> selectByAuditorId(@Param("auditorId") Long auditorId);

    /**
     * 逻辑删除审核记录（标记为已删除）
     * @param id 主键ID
     * @return 更新的行数
     */
    @Update("UPDATE news_audit_history SET is_deleted = 1 WHERE id = #{id}")
    int logicalDelete(@Param("id") Long id);

    /**
     * 获取新闻内容的最新审核记录
     * @param newsContentId 新闻内容ID
     * @return 最新审核记录
     */
    @Select("SELECT * FROM news_audit_history " +
            "WHERE news_content_id = #{newsContentId} AND is_deleted = 0 " +
            "ORDER BY created_at DESC LIMIT 1")
    @ResultMap("auditHistoryMap")
    NewsAuditHistory selectLatestByNewsContentId(@Param("newsContentId") Long newsContentId);

    /**
     * 统计指定新闻内容的审核次数
     * @param newsContentId 新闻内容ID
     * @return 审核次数
     */
    @Select("SELECT COUNT(*) FROM news_audit_history " +
            "WHERE news_content_id = #{newsContentId} AND is_deleted = 0")
    int countAuditsByContentId(@Param("newsContentId") Long newsContentId);
}