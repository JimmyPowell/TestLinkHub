package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.Lesson;
import java.util.List;

@Mapper
public interface LessonMapper {

    @Insert("INSERT INTO lesson(uuid, publisher_id, status, current_version_id, pending_version_id, is_deleted, created_at, updated_at) " +
            "VALUES(#{uuid}, #{publisherId}, #{status}, #{currentVersionId}, #{pendingVersionId}, #{isDeleted}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Lesson lesson);

    @Update("UPDATE lesson SET publisher_id=#{publisherId}, status=#{status}, current_version_id=#{currentVersionId}, pending_version_id=#{pendingVersionId}, is_deleted=#{isDeleted}, updated_at=#{updatedAt} WHERE id=#{id}")
    int update(Lesson lesson);

    @Delete("DELETE FROM lesson WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT * FROM lesson WHERE id=#{id}")
    Lesson selectById(@Param("id") Long id);

    @Select("SELECT * FROM lesson WHERE uuid=#{uuid}")
    Lesson selectByUuid(@Param("uuid") String uuid);

    @Select("SELECT * FROM lesson")
    List<Lesson> selectAll();

    // 分页动态查询
    @Select({
        "<script>",
        "SELECT * FROM lesson",
        "<where>",
        "  <if test='status != null'>AND status = #{status}</if>",
        "  <if test='publisherId != null'>AND publisher_id = #{publisherId}</if>",
        "  <if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>",
        "</where>",
        "ORDER BY created_at DESC",
        "LIMIT #{pageSize} OFFSET #{offset}",
        "</script>"
    })
    List<Lesson> selectPage(@Param("status") String status,
                            @Param("publisherId") Long publisherId,
                            @Param("isDeleted") Boolean isDeleted,
                            @Param("pageSize") int pageSize,
                            @Param("offset") int offset);

    @Select({
        "<script>",
        "SELECT COUNT(*) FROM lesson",
        "<where>",
        "  <if test='status != null'>AND status = #{status}</if>",
        "  <if test='publisherId != null'>AND publisher_id = #{publisherId}</if>",
        "  <if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>",
        "</where>",
        "</script>"
    })
    int countPage(@Param("status") String status,
                  @Param("publisherId") Long publisherId,
                  @Param("isDeleted") Boolean isDeleted);
}
