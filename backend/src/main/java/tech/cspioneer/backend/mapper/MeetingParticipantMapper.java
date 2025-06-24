package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

}
