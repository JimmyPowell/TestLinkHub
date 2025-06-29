package tech.cspioneer.backend.mapper;

import org.apache.ibatis.jdbc.SQL;

public class MeetingParticipantSqlProvider {

    public String findPartsByCreator(Long creatorId, String status) {
        return new SQL() {{
            SELECT(
                "mp.uuid",
                "mp.join_reason",
                "mp.status",
                "mp.created_at",
                "m.uuid as meetingUuid",
                "mv.name as meetingName",
                "u.name as applicantName",
                "u.uuid as applicantUuid",
                "u.email as applicantEmail"
            );
            FROM("meeting_participant mp");
            JOIN("meeting m ON mp.meeting_id = m.id");
            JOIN("user u ON mp.user_id = u.id");
            LEFT_OUTER_JOIN("meeting_version mv ON mv.id = (SELECT id FROM meeting_version WHERE meeting_id = m.id ORDER BY version DESC LIMIT 1)");
            WHERE("m.creator_id = #{creatorId}");
            if (status != null && !status.isEmpty()) {
                WHERE("mp.status = #{status}");
            }
            ORDER_BY("mp.created_at DESC");
            // LIMIT and OFFSET are handled by the mapper parameters, not in the provider
        }}.toString() + " LIMIT #{limit} OFFSET #{offset}";
    }
}
