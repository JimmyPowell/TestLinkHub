package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tech.cspioneer.backend.entity.MeetingParticipant;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MeetingParticipantMapper {

    /**
     * 根据会议ID查询所有状态为'approved'的参会者的用户ID
     * @param meetingId 会议ID
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM meeting_participant WHERE meeting_id = #{meetingId} AND status = 'approved'")
    List<Long> findUserIdsByMeetingId(@Param("meetingId") Long meetingId);


    /**
     * 根据 uuid 查询参会记录
     */
    @Select("""
        SELECT * FROM meeting_participant WHERE id = (
            SELECT id FROM meeting_participant WHERE uuid = #{uuid} LIMIT 1
        )
    """)
    MeetingParticipant findByUuid(@Param("uuid") String uuid);

    /**
     * 审核更新参会状态
     */
    @Update("""
        UPDATE meeting_participant
        SET status = #{status},
            review_comment = #{comment},
            updated_at = NOW()
        WHERE id = #{id}
    """)
    void updateReviewResult(@Param("id") Long id,
                            @Param("status") String status,
                            @Param("comment") String comment);


    /**
     * 查询某个会议创建者下所有会议的参会申请（分页）
     * 假设你有 meeting 表，creator_uuid 字段为会议创建人
     */
    @Select("""
        SELECT mp.*
        FROM meeting_participant mp
        JOIN meeting m ON mp.meeting_id = m.id
        WHERE m.creator_uuid = #{creatorUuid}
        ORDER BY mp.created_at DESC
        LIMIT #{limit} OFFSET #{offset}
    """)
    List<MeetingParticipant> findPartsByCreator(@Param("creatorUuid") String creatorUuid,
                                                @Param("offset") int offset,
                                                @Param("limit") int limit);


    @Select("""
    SELECT * FROM meeting_participant
    WHERE uuid = #{partUuid}
    LIMIT 1
""")
    MeetingParticipant findPartByUuid(@Param("partUuid") String partUuid);


}
