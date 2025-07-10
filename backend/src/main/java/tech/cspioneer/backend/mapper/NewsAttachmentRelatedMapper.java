package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.NewsAttachmentRelated;

import java.util.List;

@Mapper
public interface NewsAttachmentRelatedMapper {

    /**
     * 插入关联记录
     * @param related 关联对象
     * @return 插入的行数
     */
    @Insert("INSERT INTO news_attachment_related(news_content_id, attachment_id) " +
            "VALUES(#{newsContentId}, #{attachmentId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(NewsAttachmentRelated related);

    /**
     * 根据主键ID查询
     * @param id 主键ID
     * @return 关联对象
     */
    @Select("SELECT id, news_content_id, attachment_id, is_deleted, created_at " +
            "FROM news_attachment_related " +
            "WHERE id = #{id}")
    @Results(id = "baseResultMap", value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "news_content_id", property = "newsContentId"),
            @Result(column = "attachment_id", property = "attachmentId"),
            @Result(column = "is_deleted", property = "isDeleted"),
            @Result(column = "created_at", property = "createdAt")
    })
    NewsAttachmentRelated selectById(@Param("id") Long id);

    /**
     * 根据新闻内容ID查询关联记录
     * @param newsContentId 新闻内容ID
     * @return 关联列表
     */
    @Select("SELECT id, news_content_id, attachment_id, is_deleted, created_at " +
            "FROM news_attachment_related " +
            "WHERE news_content_id = #{newsContentId} AND is_deleted = 0")
    @ResultMap("baseResultMap")
    List<NewsAttachmentRelated> selectByNewsContentId(@Param("newsContentId") Long newsContentId);

    /**
     * 根据附件ID查询关联记录
     * @param attachmentId 附件ID
     * @return 关联列表
     */
    @Select("SELECT id, news_content_id, attachment_id, is_deleted, created_at " +
            "FROM news_attachment_related " +
            "WHERE attachment_id = #{attachmentId} AND is_deleted = 0")
    @ResultMap("baseResultMap")
    List<NewsAttachmentRelated> selectByAttachmentId(@Param("attachmentId") Long attachmentId);

    /**
     * 逻辑删除关联记录
     * @param newsContentId 新闻内容ID
     * @param attachmentId 附件ID
     * @return 更新的行数
     */
    @Update("UPDATE news_attachment_related " +
            "SET is_deleted = 1 " +
            "WHERE news_content_id = #{newsContentId} " +
            "AND attachment_id = #{attachmentId} " +
            "AND is_deleted = 0")
    int logicalDelete(@Param("newsContentId") Long newsContentId,
                      @Param("attachmentId") Long attachmentId);

    /**
     * 物理删除关联记录
     * @param id 主键ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM news_attachment_related WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 检查唯一键是否存在
     * @param newsContentId 新闻内容ID
     * @param attachmentId 附件ID
     * @return 存在数量
     */
    @Select("SELECT COUNT(*) FROM news_attachment_related " +
            "WHERE news_content_id = #{newsContentId} " +
            "AND attachment_id = #{attachmentId} " +
            "AND is_deleted = 0")
    int countUniqueKey(@Param("newsContentId") Long newsContentId,
                       @Param("attachmentId") Long attachmentId);
}