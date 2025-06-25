package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.LessonVersion;
import java.util.List;

@Mapper
public interface LessonVersionMapper {

    @Insert("INSERT INTO lesson_version(uuid, lesson_id, version, name, description, image_url, author_name, sort_order, status, creator_id, is_deleted, created_at) " +
            "VALUES(#{uuid}, #{lessonId}, #{version}, #{name}, #{description}, #{imageUrl}, #{authorName}, #{sortOrder}, #{status}, #{creatorId}, #{isDeleted}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LessonVersion lessonVersion);

    @Update("UPDATE lesson_version SET lesson_id=#{lessonId}, version=#{version}, name=#{name}, description=#{description}, image_url=#{imageUrl}, author_name=#{authorName}, sort_order=#{sortOrder}, status=#{status}, creator_id=#{creatorId}, is_deleted=#{isDeleted} WHERE id=#{id}")
    int update(LessonVersion lessonVersion);

    @Delete("DELETE FROM lesson_version WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT * FROM lesson_version WHERE id=#{id}")
    LessonVersion selectById(@Param("id") Long id);

    @Select("SELECT * FROM lesson_version WHERE uuid=#{uuid}")
    LessonVersion selectByUuid(@Param("uuid") String uuid);

    @Select("SELECT * FROM lesson_version WHERE lesson_id=#{lessonId}")
    List<LessonVersion> selectByLessonId(@Param("lessonId") Long lessonId);

    @Select("SELECT * FROM lesson_version")
    List<LessonVersion> selectAll();

    // 分页动态查询
    @Select({
        "<script>",
        "SELECT * FROM lesson_version",
        "<where>",
        "  <if test='status != null'>AND status = #{status}</if>",
        "  <if test='lessonId != null'>AND lesson_id = #{lessonId}</if>",
        "  <if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>",
        "</where>",
        "ORDER BY created_at DESC",
        "LIMIT #{pageSize} OFFSET #{offset}",
        "</script>"
    })
    List<LessonVersion> selectPage(@Param("status") String status,
                                   @Param("lessonId") Long lessonId,
                                   @Param("isDeleted") Boolean isDeleted,
                                   @Param("pageSize") int pageSize,
                                   @Param("offset") int offset);

    @Select({
        "<script>",
        "SELECT COUNT(*) FROM lesson_version",
        "<where>",
        "  <if test='status != null'>AND status = #{status}</if>",
        "  <if test='lessonId != null'>AND lesson_id = #{lessonId}</if>",
        "  <if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>",
        "</where>",
        "</script>"
    })
    int countPage(@Param("status") String status,
                  @Param("lessonId") Long lessonId,
                  @Param("isDeleted") Boolean isDeleted);
}
