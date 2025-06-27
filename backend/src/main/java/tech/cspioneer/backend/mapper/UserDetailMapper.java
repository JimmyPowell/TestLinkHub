package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.config.typehandler.GenderTypeHandler;
import tech.cspioneer.backend.config.typehandler.UserRoleTypeHandler;
import tech.cspioneer.backend.config.typehandler.UserStatusTypeHandler;
import tech.cspioneer.backend.entity.dto.response.UserDetailResponse;

import java.util.Optional;

@Mapper
public interface UserDetailMapper {

    @Select("""
            SELECT
                u.uuid,
                u.name,
                u.email,
                u.phone_number,
                u.address,
                u.avatar_url,
                u.gender,
                c.name AS company_name,
                u.role,
                u.status,
                u.description,
                u.post_count,
                u.lesson_count,
                u.meeting_count,
                u.created_at,
                u.updated_at
            FROM
                `user` u
            LEFT JOIN
                `company` c ON u.company_id = c.id
            WHERE
                u.uuid = #{uuid}
            """)
    @Results({
            @Result(property = "uuid", column = "uuid"),
            @Result(property = "name", column = "name"),
            @Result(property = "email", column = "email"),
            @Result(property = "phoneNumber", column = "phone_number"),
            @Result(property = "address", column = "address"),
            @Result(property = "avatarUrl", column = "avatar_url"),
            @Result(property = "gender", column = "gender", typeHandler = GenderTypeHandler.class),
            @Result(property = "companyName", column = "company_name"),
            @Result(property = "role", column = "role", typeHandler = UserRoleTypeHandler.class),
            @Result(property = "status", column = "status", typeHandler = UserStatusTypeHandler.class),
            @Result(property = "description", column = "description"),
            @Result(property = "postCount", column = "post_count"),
            @Result(property = "lessonCount", column = "lesson_count"),
            @Result(property = "meetingCount", column = "meeting_count"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<UserDetailResponse> findUserDetailByUuid(@Param("uuid") String uuid);
}
