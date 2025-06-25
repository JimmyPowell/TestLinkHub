package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.News;
import tech.cspioneer.backend.entity.NewsContent;

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

    // 分页查询
    @Select({
            "SELECT * FROM news",
            "WHERE company_id = #{companyId} AND is_deleted = 0",
            "ORDER BY created_at DESC",
            "LIMIT #{limit} OFFSET #{offset}"
    })
    @ResultMap("newsResultMap")
    List<News> findByCompanyIdWithPagination(
            @Param("companyId") Long companyId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    // 统计公司新闻数量
    @Select("SELECT COUNT(*) FROM news WHERE company_id = #{companyId} AND is_deleted = 0")
    int countByCompanyId(@Param("companyId") Long companyId);
}