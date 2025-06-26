package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.Lesson;
import java.util.List;
import java.util.Map;

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

    @Select({
        "<script>",
        "SELECT * FROM lesson",
        "<where>",
        "  <if test='name != null and name != \"\"'>AND name LIKE CONCAT('%', #{name}, '%')</if>",
        "  <if test='authorName != null and authorName != \"\"'>AND author_name LIKE CONCAT('%', #{authorName}, '%')</if>",
        "  <if test='startTime != null'>AND created_at &gt;= #{startTime}</if>",
        "  <if test='endTime != null'>AND created_at &lt;= #{endTime}</if>",
        "  <if test='status != null'>AND status = #{status}</if>",
        "  <if test='publisherId != null'>AND publisher_id = #{publisherId}</if>",
        "  <if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>",
        "</where>",
        "ORDER BY created_at DESC",
        "LIMIT #{pageSize} OFFSET #{offset}",
        "</script>"
    })
    List<Lesson> selectPageAdvanced(@Param("name") String name,
                                    @Param("authorName") String authorName,
                                    @Param("startTime") String startTime,
                                    @Param("endTime") String endTime,
                                    @Param("status") String status,
                                    @Param("publisherId") Long publisherId,
                                    @Param("isDeleted") Boolean isDeleted,
                                    @Param("pageSize") int pageSize,
                                    @Param("offset") int offset);

    @Select({
        "<script>",
        "SELECT",
        "  l.id AS lesson_id,",
        "  v.name AS name,",
        "  v.image_url AS image_url,",
        "  v.description AS description,",
        "  v.author_name AS author_name,",
        "  v.version AS version",
        "FROM lesson l",
        "LEFT JOIN lesson_version v ON l.current_version_id = v.id",
        "<where>",
        "  l.is_deleted = 0",
        "  AND l.current_version_id IS NOT NULL",
        "  <if test='name != null and name != \"\"'>AND v.name LIKE CONCAT('%', #{name}, '%')</if>",
        "  <if test='authorName != null and authorName != \"\"'>AND v.author_name LIKE CONCAT('%', #{authorName}, '%')</if>",
        "  <if test='beginTime != null'>AND l.created_at &gt;= #{beginTime}</if>",
        "  <if test='endTime != null'>AND l.created_at &lt;= #{endTime}</if>",
        "</where>",
        "ORDER BY l.created_at DESC",
        "LIMIT #{pageSize} OFFSET #{offset}",
        "</script>"
    })
    List<Map<String, Object>> selectLessonWithCurrentVersion(
        @Param("name") String name,
        @Param("authorName") String authorName,
        @Param("beginTime") String beginTime,
        @Param("endTime") String endTime,
        @Param("pageSize") int pageSize,
        @Param("offset") int offset
    );

    @Select({
        "<script>",
        "SELECT",
        "  l.id AS lesson_id,",
        "  v.name AS name,",
        "  v.image_url AS image_url,",
        "  v.description AS description,",
        "  v.author_name AS author_name,",
        "  v.version AS version",
        "FROM lesson l",
        "LEFT JOIN lesson_version v ON l.current_version_id = v.id",
        "<where>",
        "  l.is_deleted = 0",
        "  AND l.current_version_id IS NOT NULL",
        "  <if test='keyword != null and keyword != \"\"'>",
        "    AND (v.name LIKE CONCAT('%', #{keyword}, '%')",
        "         OR v.author_name LIKE CONCAT('%', #{keyword}, '%')",
        "         OR v.description LIKE CONCAT('%', #{keyword}, '%'))",
        "  </if>",
        "</where>",
        "ORDER BY l.created_at DESC",
        "LIMIT #{pageSize} OFFSET #{offset}",
        "</script>"
    })
    List<Map<String, Object>> searchLessonWithCurrentVersion(@Param("keyword") String keyword,
                                                            @Param("pageSize") int pageSize,
                                                            @Param("offset") int offset);

    @Update({
        "<script>",
        "UPDATE lesson SET is_deleted=1 WHERE id IN",
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
        "  #{id}",
        "</foreach>",
        "</script>"
    })
    int softDeleteLessons(@Param("ids") List<Long> ids);
}
