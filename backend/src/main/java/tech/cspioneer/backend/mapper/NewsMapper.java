package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.News;
import tech.cspioneer.backend.entity.NewsContent;
import tech.cspioneer.backend.entity.dto.response.NewsAuditDetailResponse;
import tech.cspioneer.backend.entity.dto.response.NewsAuditListResponse;
import tech.cspioneer.backend.entity.dto.response.NewsHistoryResponse;
import tech.cspioneer.backend.entity.dto.response.NewsListResponse;
import tech.cspioneer.backend.entity.query.NewsListQuery;

import java.util.List;

@Mapper
public interface NewsMapper {

    // 插入新闻并返回自增ID
    @Insert({
            "INSERT INTO news (uuid, company_id, visible, status, ",
            "current_content_id, pending_content_id, is_deleted, ",
            "created_at, updated_at) ",
            "VALUES (#{uuid}, #{companyId}, #{visible}, #{status}, ",
            "#{currentContentId}, #{pendingContentId}, #{isDeleted}, ",
            "#{createdAt}, #{updatedAt})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(News news);

    // 更新新闻
    @Update({
            "<script>",
            "UPDATE news",
            "<set>",
            "   <if test='visible != null'>visible = #{visible},</if>",
            "   <if test='status != null'>status = #{status},</if>",
            "   <if test='currentContentId != null'>current_content_id = #{currentContentId},</if>",
            "   <if test='pendingContentId != null'>pending_content_id = #{pendingContentId},</if>",
            "   <if test='isDeleted != null'>is_deleted = #{isDeleted},</if>",
            "   updated_at = NOW()",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    int update(News news);

    // 逻辑删除
    @Update("UPDATE news SET is_deleted = 1, updated_at = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    // 根据ID查询
    @Select("SELECT * FROM news WHERE id = #{id} AND is_deleted = 0")
    @Results(id = "newsResultMap", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "companyId", column = "company_id"),
            @Result(property = "visible", column = "visible"),
            @Result(property = "status", column = "status"),
            @Result(property = "currentContentId", column = "current_content_id"),
            @Result(property = "pendingContentId", column = "pending_content_id"),
            @Result(property = "isDeleted", column = "is_deleted"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    News findById(@Param("id") Long id);

    // 根据UUID查询
    @Select("SELECT * FROM news WHERE uuid = #{uuid} AND is_deleted = 0")
    @ResultMap("newsResultMap")
    News findByUuid(@Param("uuid") String uuid);

    // 根据公司ID查询
    @Select({
            "SELECT * FROM news",
            "WHERE company_id = #{companyId} AND is_deleted = 0",
            "ORDER BY created_at DESC"
    })
    @ResultMap("newsResultMap")
    List<News> findByCompanyId(@Param("companyId") Long companyId);

    // 根据状态查询
    @Select({
            "SELECT * FROM news",
            "WHERE status = #{status} AND is_deleted = 0",
            "ORDER BY created_at DESC"
    })
    @ResultMap("newsResultMap")
    List<News> findByStatus(@Param("status") String status);

    // 根据可见性和公司ID查询
    @Select({
            "SELECT * FROM news",
            "WHERE visible = #{visible}",
            "AND company_id = #{companyId}",
            "AND is_deleted = 0",
            "ORDER BY created_at DESC"
    })
    @ResultMap("newsResultMap")
    List<News> findByVisibilityAndCompany(
            @Param("visible") Integer visible,
            @Param("companyId") Long companyId);

    // 查询待审核的新闻
    @Select({
            "SELECT * FROM news",
            "WHERE status = 'pending'",
            "AND is_deleted = 0",
            "ORDER BY created_at DESC"
    })
    @ResultMap("newsResultMap")
    List<News> findPendingNews();

    // 更新新闻状态
    @Update("UPDATE news SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(
            @Param("id") Long id,
            @Param("status") String status);

    // 更新内容ID
    @Update("UPDATE news SET current_content_id = #{currentContentId}, pending_content_id = #{pendingContentId}, updated_at = NOW() WHERE id = #{id}")
    int updateContentIds(
            @Param("id") Long id,
            @Param("currentContentId") Long currentContentId,
            @Param("pendingContentId") Long pendingContentId);

    // 分页查询(支持查询所有新闻或按公司ID过滤)
    @Select({
            "<script>",
            "SELECT * FROM news",
            "WHERE is_deleted = 0",
            "<if test='companyId != null'>AND company_id = #{companyId}</if>",
            "ORDER BY created_at DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    @ResultMap("newsResultMap")
    List<News> findByCompanyIdWithPagination(
            @Param("companyId") Long companyId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    // 统计新闻数量(支持统计所有新闻或按公司ID过滤)
    @Select({
            "<script>",
            "SELECT COUNT(*) FROM news",
            "WHERE is_deleted = 0",
            "<if test='companyId != null'>AND company_id = #{companyId}</if>",
            "</script>"
    })
    int countNews(@Param("companyId") Long companyId);

    // 联查新闻列表
    @Select({
            "<script>",
            "SELECT n.uuid, n.company_id as companyId, nc.created_at as contentCreatedAt, ",
            "nc.cover_image_url as coverImageUrl, n.created_at as createdAt, ",
            "nc.publisher_id as publisherId, nc.summary, nc.title ",
            "FROM news n ",
            "JOIN news_content nc ON n.current_content_id = nc.id ",
            "WHERE n.is_deleted = 0 AND nc.is_deleted = 0 ",
            "<if test='query.companyUuid != null'>",
            "   AND (n.visible = 1 OR n.company_id = (SELECT id FROM company WHERE uuid = #{query.companyUuid})) ",
            "</if>",
            "<if test='query.title != null and query.title != \"\"'>",
            "   AND nc.title LIKE CONCAT('%', #{query.title}, '%') ",
            "</if>",
            "<if test='query.summary != null and query.summary != \"\"'>",
            "   AND nc.summary LIKE CONCAT('%', #{query.summary}, '%') ",
            "</if>",
            "<if test='query.startTime != null and query.startTime != \"\"'>",
            "   AND n.created_at <![CDATA[ >= ]]> #{query.startTime} ",
            "</if>",
            "<if test='query.endTime != null and query.endTime != \"\"'>",
            "   AND n.created_at <![CDATA[ <= ]]> #{query.endTime} ",
            "</if>",
            "ORDER BY n.created_at DESC ",
            "LIMIT #{query.pageSize} OFFSET #{query.pageSize} * (#{query.page} - 1)",
            "</script>"
    })
    List<NewsListResponse> findNewsList(@Param("query") NewsListQuery query);

    // 查询待审核新闻列表
    @Select({
            "SELECT n.uuid, n.company_id as companyId, nc.created_at as contentCreatedAt, ",
            "nc.cover_image_url as coverImageUrl, n.created_at as createdAt, ",
            "nc.publisher_id as publisherId, nc.summary, nc.title, nc.version ",
            "FROM news n ",
            "JOIN news_content nc ON n.pending_content_id = nc.id ",
            "WHERE n.is_deleted = 0 AND nc.is_deleted = 0 ",
            "AND n.status = 'pending' ",
            "ORDER BY n.created_at DESC ",
            "LIMIT #{pageSize} OFFSET #{pageSize} * (#{page} - 1)"
    })
    List<NewsAuditListResponse> findPendingNewsList(
            @Param("page") int page,
            @Param("pageSize") int pageSize);

    // 查询待审核新闻详情
    @Select({
            "SELECT n.uuid, n.company_id as companyId, nc.created_at as contentCreatedAt, ",
            "nc.cover_image_url as coverImageUrl, n.created_at as createdAt, ",
            "nc.publisher_id as publisherId, nc.resource_url as resourceUrl, ",
            "nc.summary, nc.title, nc.version ",
            "FROM news n ",
            "JOIN news_content nc ON n.pending_content_id = nc.id ",
            "WHERE n.uuid = #{uuid} AND n.is_deleted = 0 AND nc.is_deleted = 0 ",
            "AND n.status = 'pending'"
    })
    NewsAuditDetailResponse findPendingNewsDetail(@Param("uuid") String uuid);

    // 查询新闻历史记录
    @Select({
        "SELECT nh.uuid, nh.audit_status as auditStatus, nh.auditor_uuid as auditorUUid, ",
        "nh.comments, n.company_id as companyId, nc.created_at as contentCreatedAt, ",
        "nc.cover_image_url as coverImageUrl, nh.created_at as createdAt, ",
        "nc.uuid as newsContentUUid, nc.publisher_id as publisherId, ",
        "nc.summary, nc.title, nc.version ",
        "FROM news n ",
        "JOIN news_content nc ON n.id = nc.news_id ",
        "JOIN news_audit_history nh ON nc.id = nh.news_content_id ",
        "WHERE n.uuid = #{uuid} ",
        "ORDER BY nh.id DESC"
    })
    List<NewsHistoryResponse> findNewsHistory(@Param("uuid") String uuid);
}
