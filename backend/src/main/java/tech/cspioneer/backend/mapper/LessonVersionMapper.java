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

    @Select("SELECT * FROM lesson_version WHERE id=#{id} AND is_deleted = 0")
    LessonVersion selectById(@Param("id") Long id);

    @Select("SELECT * FROM lesson_version WHERE uuid=#{uuid} AND is_deleted = 0")
    LessonVersion selectByUuid(@Param("uuid") String uuid);

    @Select("SELECT * FROM lesson_version WHERE lesson_id=#{lessonId} AND is_deleted = 0")
    List<LessonVersion> selectAllByLessonId(@Param("lessonId") Long lessonId);

    @Update({
        "<script>",
        "UPDATE lesson_version SET is_deleted=1 WHERE lesson_id IN",
        "<foreach collection='lessonIds' item='id' open='(' separator=',' close=')'>",
        "  #{id}",
        "</foreach>",
        "</script>"
    })
    int softDeleteVersionsByLessonIds(@Param("lessonIds") List<Long> lessonIds);
}
