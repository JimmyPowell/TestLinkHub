package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Mapper;
import tech.cspioneer.backend.entity.NewsContent;
import java.util.List;


@Mapper
public interface NewsContentMapper {

    // 插入新闻内容并返回自增ID
    @Insert({
            "INSERT INTO news_content (uuid, news_id, title, summary, ",
            "cover_image_url, resource_url, version, status, ",
            "publisher_id, is_deleted) ",
            "VALUES (#{uuid}, #{newsId}, #{title}, #{summary}, ",
            "#{coverImageUrl}, #{resourceUrl}, #{version}, #{status}, ",
            "#{publisherId}, #{isDeleted})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(NewsContent newsContent);

    // 更新新闻内容
    @Update({
            "<script>",
            "UPDATE news_content",
            "<set>",
            "   <if test='title != null'>title = #{title},</if>",
            "   <if test='summary != null'>summary = #{summary},</if>",
            "   <if test='coverImageUrl != null'>cover_image_url = #{coverImageUrl},</if>",
            "   <if test='resourceUrl != null'>resource_url = #{resourceUrl},</if>",
            "   <if test='version != null'>version = #{version},</if>",
            "   <if test='status != null'>status = #{status},</if>",
            "   <if test='isDeleted != null'>is_deleted = #{isDeleted},</if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    int update(NewsContent newsContent);

    // 逻辑删除
    @Update("UPDATE news_content SET is_deleted = 1 WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    // 根据ID查询
    @Select("SELECT * FROM news_content WHERE id = #{id} AND is_deleted = 0")
    @Results(id = "newsContentResultMap", value = {
            @Result(property = "id", column = "id", id = true),
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "newsId", column = "news_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "summary", column = "summary"),
            @Result(property = "coverImageUrl", column = "cover_image_url"),
            @Result(property = "resourceUrl", column = "resource_url"),
            @Result(property = "version", column = "version"),
            @Result(property = "status", column = "status"),
            @Result(property = "publisherId", column = "publisher_id"),
            @Result(property = "isDeleted", column = "is_deleted"),
            @Result(property = "createdAt", column = "created_at")
    })
    NewsContent findById(@Param("id") Long id);

    // 根据UUID查询
    @Select("SELECT * FROM news_content WHERE uuid = #{uuid} AND is_deleted = 0")
    @ResultMap("newsContentResultMap")
    NewsContent findByUuid(@Param("uuid") String uuid);

    // 根据新闻ID查询所有版本
    @Select({
            "SELECT * FROM news_content",
            "WHERE news_id = #{newsId} AND is_deleted = 0",
            "ORDER BY created_at DESC"
    })
    @ResultMap("newsContentResultMap")
    List<NewsContent> findByNewsId(@Param("newsId") Long newsId);

    // 根据新闻ID和状态查询
    @Select({
            "SELECT * FROM news_content",
            "WHERE news_id = #{newsId} AND status = #{status}",
            "AND is_deleted = 0",
            "ORDER BY created_at DESC"
    })
    @ResultMap("newsContentResultMap")
    List<NewsContent> findByNewsIdAndStatus(
            @Param("newsId") Long newsId,
            @Param("status") String status);

    // 获取指定新闻的最新版本
    @Select({
            "SELECT * FROM news_content",
            "WHERE news_id = #{newsId} AND is_deleted = 0",
            "ORDER BY version DESC",
            "LIMIT 1"
    })
    @ResultMap("newsContentResultMap")
    NewsContent findLatestVersionByNewsId(@Param("newsId") Long newsId);

    // 获取待审核的内容版本
    @Select({
            "SELECT * FROM news_content",
            "WHERE status = 'pending' AND is_deleted = 0",
            "ORDER BY created_at DESC"
    })
    @ResultMap("newsContentResultMap")
    List<NewsContent> findPendingContents();

    // 更新内容状态
    @Update("UPDATE news_content SET status = #{status} WHERE id = #{id}")
    int updateStatus(
            @Param("id") Long id,
            @Param("status") String status);

    // 获取指定新闻的最大版本号
    @Select("SELECT COALESCE(MAX(version), 0) FROM news_content WHERE news_id = #{newsId}")
    int getMaxVersionByNewsId(@Param("newsId") Long newsId);
}