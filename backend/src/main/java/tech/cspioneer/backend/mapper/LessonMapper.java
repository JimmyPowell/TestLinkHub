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

    @Select("SELECT * FROM lesson WHERE uuid=#{uuid}")
    Lesson selectByUuid(@Param("uuid") String uuid);

    @Select({
        "<script>",
        "SELECT",
        "  l.uuid AS uuid,",
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
        "  l.uuid AS uuid,",
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

    @Select({
        "<script>",
        "SELECT",
        "  l.uuid AS uuid,",
        "  v.name AS name,",
        "  c.name AS company_name,",
        "  v.author_name AS author_name,",
        "  v.created_at AS created_at,",
        "  l.status AS status",
        "FROM lesson l",
        "JOIN lesson_version v ON v.id = (CASE WHEN l.status = 'pending_review' THEN l.pending_version_id ELSE l.current_version_id END)",
        "JOIN company c ON l.publisher_id = c.id",
        "<where>",
        "  l.is_deleted = 0",
        "  <if test='status != null and status != \"\"'>",
        "    AND l.status = #{status}",
        "  </if>",
        "  <if test='name != null and name != \"\"'>",
        "    AND v.name LIKE CONCAT('%', #{name}, '%')",
        "  </if>",
        "  <if test='companyName != null and companyName != \"\"'>",
        "    AND c.name LIKE CONCAT('%', #{companyName}, '%')",
        "  </if>",
        "</where>",
        "ORDER BY l.updated_at DESC",
        "LIMIT #{pageSize} OFFSET #{offset}",
        "</script>"
    })
    List<Map<String, Object>> selectReviewLessons(@Param("pageSize") int pageSize, @Param("offset") int offset, @Param("status") String status, @Param("name") String name, @Param("companyName") String companyName);

    @Select({
        "<script>",
        "SELECT",
        "  l.uuid AS uuid,",
        "  v.name AS name,",
        "  v.image_url AS imageUrl,",
        "  v.description AS description,",
        "  v.author_name AS authorName,",
        "  v.version AS version,",
        "  v.status AS status,",
        "  v.created_at AS createdAt",
        "FROM lesson l",
        "LEFT JOIN lesson_version v ON l.pending_version_id = v.id",
        "<where>",
        "  l.is_deleted = 0",
        "  AND l.status = 'pending_review'",
        "  AND l.pending_version_id IS NOT NULL",
        "  AND l.publisher_id = #{companyId}",
        "  <if test='name != null and name != \"\"'>AND v.name LIKE CONCAT('%', #{name}, '%')</if>",
        "  <if test='status != null and status != \"\"'>AND v.status = #{status}</if>",
        "  <if test='lessonUuid != null and lessonUuid != \"\"'>AND l.uuid = #{lessonUuid}</if>",
        "</where>",
        "ORDER BY l.updated_at DESC",
        "LIMIT #{pageSize} OFFSET #{offset}",
        "</script>"
    })
    List<Map<String, Object>> selectCompanyPendingReviewLessonsOverview(
        @Param("name") String name,
        @Param("status") String status,
        @Param("lessonUuid") String lessonUuid,
        @Param("companyId") Long companyId,
        @Param("pageSize") int pageSize,
        @Param("offset") int offset
    );

    @Select({
        "<script>",
        "SELECT COUNT(*)",
        "FROM lesson l",
        "LEFT JOIN lesson_version v ON l.pending_version_id = v.id",
        "<where>",
        "  l.is_deleted = 0",
        "  AND l.status = 'pending_review'",
        "  AND l.pending_version_id IS NOT NULL",
        "  AND l.publisher_id = #{companyId}",
        "  <if test='name != null and name != \"\"'>AND v.name LIKE CONCAT('%', #{name}, '%')</if>",
        "  <if test='status != null and status != \"\"'>AND v.status = #{status}</if>",
        "  <if test='lessonUuid != null and lessonUuid != \"\"'>AND l.uuid = #{lessonUuid}</if>",
        "</where>",
        "</script>"
    })
    int countCompanyPendingReviewLessonsOverview(
        @Param("name") String name,
        @Param("status") String status,
        @Param("lessonUuid") String lessonUuid,
        @Param("companyId") Long companyId
    );

    @Select({
        "<script>",
        "SELECT",
        "  l.uuid AS uuid,",
        "  v.name AS name,",
        "  v.image_url AS image_url,",
        "  v.description AS description,",
        "  v.author_name AS author_name,",
        "  v.version AS version,",
        "  l.status as status,",
        "  l.updated_at as updated_at",
        "FROM lesson l",
        "LEFT JOIN lesson_version v ON (l.status = 'active' AND v.id = l.current_version_id) OR (l.status = 'pending_review' AND v.id = l.pending_version_id)",
        "WHERE l.publisher_id = (SELECT id FROM company WHERE uuid = #{companyUuid})",
        "  AND l.is_deleted = 0",
        "  AND v.id IS NOT NULL",
        "  <if test='lessonUuid != null and lessonUuid != \"\"'>AND l.uuid = #{lessonUuid}</if>",
        "  <if test='name != null and name != \"\"'>AND v.name LIKE CONCAT('%', #{name}, '%')</if>",
        "  <if test='status != null and status != \"\"'>AND l.status = #{status}</if>",
        "ORDER BY l.updated_at DESC",
        "LIMIT #{limit} OFFSET #{offset}",
        "</script>"
    })
    List<Map<String, Object>> findLessonsByCompanyUuidWithVersion(@Param("companyUuid") String companyUuid,
                                                                  @Param("lessonUuid") String lessonUuid,
                                                                  @Param("name") String name,
                                                                  @Param("status") String status,
                                                                  @Param("limit") int limit,
                                                                  @Param("offset") int offset);

    @Select({
        "<script>",
        "SELECT COUNT(*)",
        "FROM lesson l",
        "LEFT JOIN lesson_version v ON (l.status = 'active' AND v.id = l.current_version_id) OR (l.status = 'pending_review' AND v.id = l.pending_version_id)",
        "WHERE l.publisher_id = (SELECT id FROM company WHERE uuid = #{companyUuid})",
        "  AND l.is_deleted = 0",
        "  AND v.id IS NOT NULL",
        "  <if test='lessonUuid != null and lessonUuid != \"\"'>AND l.uuid = #{lessonUuid}</if>",
        "  <if test='name != null and name != \"\"'>AND v.name LIKE CONCAT('%', #{name}, '%')</if>",
        "  <if test='status != null and status != \"\"'>AND l.status = #{status}</if>",
        "</script>"
    })
    long countLessonsByCompanyUuid(@Param("companyUuid") String companyUuid,
                                   @Param("lessonUuid") String lessonUuid,
                                   @Param("name") String name,
                                   @Param("status") String status);
}
