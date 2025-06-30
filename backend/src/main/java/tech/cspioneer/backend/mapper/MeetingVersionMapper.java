package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.MeetingVersion;
import tech.cspioneer.backend.entity.dto.response.MeetingVersionWithMeetingUuidResponse;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MeetingVersionMapper {

    //创建新的会议版本
    @Insert("""
        INSERT INTO meeting_version (
            uuid,
            meeting_id,
            version,
            name,
            description,
            cover_image_url,
            start_time,
            end_time,
            status,
            editor_id,
            created_at,
            is_deleted
        ) VALUES (
            #{uuid},
            #{meetingId},
            #{version},
            #{name},
            #{description},
            #{coverImageUrl},
            #{startTime},
            #{endTime},
            #{status},
            #{editorId},
            #{createdAt},
            0
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    public void insert(MeetingVersion meetingVersion) ;

    //查找最大版本号
    @Select("SELECT MAX(version) FROM meeting_version WHERE meeting_id = #{meetingId} AND is_deleted = 0")
    public Integer findMaxVersionByMeetingId(Long id) ;

    @Select("SELECT * FROM meeting_version WHERE uuid = #{uuid} LIMIT 1")
    MeetingVersion findByUuid(@Param("uuid") String uuid);

    @Update("UPDATE meeting_version SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);




    //根据传入的id找出对应的version表
    @Select({
            "<script>",
            "SELECT * FROM meeting_version WHERE id IN",
            "<foreach item='id' index='index' collection='ids' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "AND is_deleted = 0",
            "</script>"
    })
    List<MeetingVersion> findVersionsByIds(@Param("ids") List<Long> ids);

    @Select("SELECT * FROM meeting_version WHERE id = #{id} AND is_deleted = 0")
    MeetingVersion findById(@Param("id") Long id);

    @Select({
            "<script>",
            "SELECT * FROM meeting_version",
            "WHERE is_deleted = 0",
            "<if test='status != null and !status.isEmpty()'>",
            "  AND status = #{status}",
            "</if>",
            "<if test='name != null and !name.isEmpty()'>",
            "  AND name LIKE CONCAT('%', #{name}, '%')",
            "</if>",
            "ORDER BY created_at DESC",
            "LIMIT #{size} OFFSET #{offset}",
            "</script>"
    })
    List<MeetingVersion> findPendingList(@Param("offset") int offset, @Param("size") int size, @Param("status") String status, @Param("name") String name);

    @Select({
            "<script>",
            "SELECT * FROM meeting_version WHERE meeting_id IN",
            "<foreach collection='meetingIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "AND is_deleted = 0",
            "</script>"
    })
    List<MeetingVersion> findVersionsByMeetingIds(@Param("meetingIds") List<Long> meetingIds);

    @SelectProvider(type = MeetingVersionSqlProvider.class, method = "findMeetingVersionsByCreatorSql")
    List<MeetingVersionWithMeetingUuidResponse> findMeetingVersionsByCreator(
            @Param("creatorId") Long creatorId,
            @Param("name") String name,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("offset") int offset,
            @Param("size") int size
    );

    @Update("UPDATE meeting_version SET is_deleted = 1 WHERE meeting_id = #{meetingId}")
    void softDeleteByMeetingId(@Param("meetingId") Long meetingId);



    // 1. 按名称模糊查询
    @Select("SELECT * FROM meeting_version " +
            "WHERE name LIKE CONCAT('%', #{name}, '%') AND is_deleted = 0 " +
            "ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<MeetingVersion> searchByName(@Param("name") String name, @Param("offset") int offset, @Param("limit") int limit);

    // 2. 按时间范围查询
    @Select("SELECT * FROM meeting_version " +
            "WHERE start_time BETWEEN #{start} AND #{end} AND is_deleted = 0 " +
            "ORDER BY start_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<MeetingVersion> searchByStartTimeRange(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end,
                                                @Param("offset") int offset,
                                                @Param("limit") int limit);

    // 3. 按 UUID 模糊查询
    @Select("SELECT * FROM meeting_version " +
            "WHERE uuid LIKE CONCAT('%', #{uuid}, '%') AND is_deleted = 0 " +
            "ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<MeetingVersion> searchByUuid(@Param("uuid") String uuid,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);
}
