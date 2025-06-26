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

    @Select("SELECT * FROM lesson_resources WHERE lesson_version_id=#{lessonVersionId} AND is_deleted = 0")
    List<LessonResources> selectByLessonVersionId(@Param("lessonVersionId") Long lessonVersionId);

    @Update({
        "<script>",
        "UPDATE lesson_resources SET is_deleted=1 WHERE lesson_version_id IN",
        "<foreach collection='versionIds' item='id' open='(' separator=',' close=')'>",
        "  #{id}",
        "</foreach>",
        "</script>"
    })
    int softDeleteResourcesByVersionIds(@Param("versionIds") List<Long> versionIds);

}
