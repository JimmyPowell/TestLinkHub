package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.MeetingParticipant;
import tech.cspioneer.backend.entity.dto.response.MeetingApplicationResponse;

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
     * 审核更新参会状态和审核意见
     */
    @Update("""
        UPDATE meeting_participant
        SET status = #{status},
            review_comment = #{reviewComment},
            updated_at = NOW()
        WHERE id = #{id}
    """)
    void updateReviewResult(@Param("id") Long id, @Param("status") String status, @Param("reviewComment") String reviewComment);


    /**
     * 查询某个会议创建者下所有会议的参会申请（分页）
     * 假设你有 meeting 表，creator_uuid 字段为会议创建人
     */
    @SelectProvider(type = MeetingParticipantSqlProvider.class, method = "findPartsByCreator")
    List<MeetingApplicationResponse> findPartsByCreator(@Param("creatorId") Long creatorId,
                                                        @Param("status") String status,
                                                        @Param("offset") int offset,
                                                        @Param("limit") int limit);


    @Select("""
    SELECT * FROM meeting_participant
    WHERE uuid = #{partUuid}
    LIMIT 1
""")
    MeetingParticipant findPartByUuid(@Param("partUuid") String partUuid);

    @Select("SELECT * FROM meeting_participant WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{size} OFFSET #{offset}")
    List<MeetingParticipant> findPartsByUser(@Param("userId") Long userId,
                                             @Param("offset") int offset,
                                             @Param("size") int size);


    @Select("SELECT * FROM meeting_participant WHERE meeting_id = #{meetingId} AND user_uuid = #{useruuid} LIMIT 1")
    MeetingParticipant findByMeetingIdAndUser(@Param("meetingId") Long meetingId,
                                              @Param("useruuid") String useruuid);
    @Insert("INSERT INTO meeting_participant (uuid, meeting_id, user_id, join_reason, created_at, status) " +
            "VALUES (#{uuid}, #{meetingId}, #{userId}, #{joinReason}, #{createdAt}, #{status})")
    void insertParticipant(MeetingParticipant participant);

    // 更新状态
    @Update("UPDATE meeting_participant SET status = #{status}, is_deleted = #{isDeleted}, updated_at = NOW() WHERE uuid = #{uuid}")
    int updateStatusAndSoftDeleteByUuid(@Param("status") String status, @Param("isDeleted") Integer isDeleted, @Param("uuid") String uuid);


    @Select("SELECT COUNT(1) FROM meeting_participant WHERE meeting_id = #{meetingId} AND user_id = #{userId}")
    int countByMeetingIdAndUserId(@Param("meetingId") Long meetingId, @Param("userId") Long userId);

    // 你也可以用默认的existsBy...风格，写成boolean返回值
    default boolean existsByMeetingIdAndUserId(Long meetingId, Long userId) {
        return countByMeetingIdAndUserId(meetingId, userId) > 0;
    }
}
