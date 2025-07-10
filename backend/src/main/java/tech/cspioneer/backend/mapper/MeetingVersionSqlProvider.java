package tech.cspioneer.backend.mapper;

import org.apache.ibatis.jdbc.SQL;
import java.time.LocalDateTime;
import java.util.Map;

public class MeetingVersionSqlProvider {

    public String findMeetingVersionsByCreatorSql(Map<String, Object> params) {
        return new SQL() {{
            SELECT("mv.*, m.uuid as meetingUuid");
            FROM("meeting_version mv");
            JOIN("meeting m ON mv.meeting_id = m.id");
            WHERE("mv.editor_id = #{creatorId}");
            WHERE("mv.is_deleted = 0");
            WHERE("m.is_deleted = 0");

            if (params.get("name") != null && !params.get("name").toString().isEmpty()) {
                WHERE("mv.name LIKE CONCAT('%', #{name}, '%')");
            }
            if (params.get("startTime") != null) {
                WHERE("mv.start_time >= #{startTime}");
            }
            if (params.get("endTime") != null) {
                WHERE("mv.start_time <= #{endTime}");
            }

            ORDER_BY("mv.created_at DESC");
            LIMIT("#{size}");
            OFFSET("#{offset}");
        }}.toString();
    }
}
