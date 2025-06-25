package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.LessonResources;
import java.util.List;

@Mapper
public interface LessonResourceMapper {

    @Insert("INSERT INTO lesson_resources(uuid, lesson_version_id, resources_url, resources_type, sort_order, status, is_deleted, created_at, updated_at) " +
            "VALUES(#{uuid}, #{lessonVersionId}, #{resourcesUrl}, #{resourcesType}, #{sortOrder}, #{status}, #{isDeleted}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LessonResources lessonResources);

    @Update("UPDATE lesson_resources SET lesson_version_id=#{lessonVersionId}, resources_url=#{resourcesUrl}, resources_type=#{resourcesType}, sort_order=#{sortOrder}, status=#{status}, is_deleted=#{isDeleted}, updated_at=#{updatedAt} WHERE id=#{id}")
    int update(LessonResources lessonResources);

    @Delete("DELETE FROM lesson_resources WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT * FROM lesson_resources WHERE id=#{id}")
    LessonResources selectById(@Param("id") Long id);

    @Select("SELECT * FROM lesson_resources WHERE uuid=#{uuid}")
    LessonResources selectByUuid(@Param("uuid") String uuid);

    @Select("SELECT * FROM lesson_resources WHERE lesson_version_id=#{lessonVersionId}")
    List<LessonResources> selectByLessonVersionId(@Param("lessonVersionId") Long lessonVersionId);

    @Select("SELECT * FROM lesson_resources")
    List<LessonResources> selectAll();

    // 分页动态查询
    @Select({
        "<script>",
        "SELECT * FROM lesson_resources",
        "<where>",
        "  <if test='status != null'>AND status = #{status}</if>",
        "  <if test='lessonVersionId != null'>AND lesson_version_id = #{lessonVersionId}</if>",
        "  <if test='resourcesType != null'>AND resources_type = #{resourcesType}</if>",
        "  <if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>",
        "</where>",
        "ORDER BY created_at DESC",
        "LIMIT #{pageSize} OFFSET #{offset}",
        "</script>"
    })
    List<LessonResources> selectPage(@Param("status") String status,
                                     @Param("lessonVersionId") Long lessonVersionId,
                                     @Param("resourcesType") String resourcesType,
                                     @Param("isDeleted") Boolean isDeleted,
                                     @Param("pageSize") int pageSize,
                                     @Param("offset") int offset);

    @Select({
        "<script>",
        "SELECT COUNT(*) FROM lesson_resources",
        "<where>",
        "  <if test='status != null'>AND status = #{status}</if>",
        "  <if test='lessonVersionId != null'>AND lesson_version_id = #{lessonVersionId}</if>",
        "  <if test='resourcesType != null'>AND resources_type = #{resourcesType}</if>",
        "  <if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>",
        "</where>",
        "</script>"
    })
    int countPage(@Param("status") String status,
                  @Param("lessonVersionId") Long lessonVersionId,
                  @Param("resourcesType") String resourcesType,
                  @Param("isDeleted") Boolean isDeleted);
}
