package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.Meeting;


@Mapper
public interface MeetingMapper {

    // 根据会议ID查找会议
    @Select("""
    SELECT * FROM meeting
    WHERE id = #{id}
    AND is_deleted = 0
    LIMIT 1
""")
    Meeting findById(@Param("id") Long id);


    //创建新的会议
    @Insert("""
        INSERT INTO meeting (
            uuid, creator_id, status, current_version_id, pending_version_id,
            created_at, updated_at, is_deleted
        ) VALUES (
            #{uuid}, #{creatorId}, #{status}, #{currentVersionId}, #{pendingVersionId},
            #{createdAt}, #{updatedAt}, #{isDeleted}
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    public void insert(Meeting meeting);

    //更新会议
    @Update("""
        UPDATE meeting SET
            status = #{status},
            current_version_id = #{currentVersionId},
            pending_version_id = #{pendingVersionId},
            updated_at = #{updatedAt},
            is_deleted = #{isDeleted}
        WHERE id = #{id}
    """)
    public void update(Meeting meeting);


    //根据uuid查找会议
    @Select("""
    SELECT * FROM meeting
    WHERE uuid = #{meetingUuid}
    AND is_deleted = 0
    LIMIT 1
""")
    Meeting findByUuid(String meetingUuid);
}
