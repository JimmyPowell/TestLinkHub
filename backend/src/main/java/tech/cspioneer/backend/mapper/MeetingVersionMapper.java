package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import tech.cspioneer.backend.entity.MeetingVersion;

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
    public void insert(MeetingVersion meetingVersion) ;

    //查找最大版本号
    @Select("SELECT MAX(version) FROM meeting_version WHERE meeting_id = #{meetingId} AND is_deleted = 0")
    public Integer findMaxVersionByMeetingId(Long id) ;
}
