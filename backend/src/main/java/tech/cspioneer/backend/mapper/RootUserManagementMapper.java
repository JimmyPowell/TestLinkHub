package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tech.cspioneer.backend.entity.dto.request.RootUserSearchRequest;
import tech.cspioneer.backend.entity.dto.response.UserResponse;

import java.util.List;

@Mapper
public interface RootUserManagementMapper {

    @Select("""
            <script>
            SELECT
                u.uuid, u.name, u.email, u.phone_number, u.address, u.avatar_url, u.gender,
                u.company_id, c.name as companyName, c.uuid as companyUuid, u.role, u.status, u.description, u.updated_at
            FROM
                user u
            LEFT JOIN
                company c ON u.company_id = c.id
            <where>
                <if test="request.uuid != null and request.uuid != ''">
                    AND u.uuid = #{request.uuid}
                </if>
                <if test="request.username != null and request.username != ''">
                    AND u.name LIKE CONCAT('%', #{request.username}, '%')
                </if>
                <if test="request.status != null and request.status != ''">
                    AND u.status = #{request.status}
                </if>
                <if test="request.phoneNumber != null and request.phoneNumber != ''">
                    AND u.phone_number LIKE CONCAT('%', #{request.phoneNumber}, '%')
                </if>
                <if test="request.companyName != null and request.companyName != ''">
                    AND c.name LIKE CONCAT('%', #{request.companyName}, '%')
                </if>
                <if test="request.companyUuid != null and request.companyUuid != ''">
                    AND c.uuid = #{request.companyUuid}
                </if>
                AND u.is_deleted = 0
            </where>
            ORDER BY u.updated_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<UserResponse> findUsers(@Param("request") RootUserSearchRequest request, @Param("offset") int offset, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT
                COUNT(*)
            FROM
                user u
            LEFT JOIN
                company c ON u.company_id = c.id
            <where>
                <if test="request.uuid != null and request.uuid != ''">
                    AND u.uuid = #{request.uuid}
                </if>
                <if test="request.username != null and request.username != ''">
                    AND u.name LIKE CONCAT('%', #{request.username}, '%')
                </if>
                <if test="request.status != null and request.status != ''">
                    AND u.status = #{request.status}
                </if>
                <if test="request.phoneNumber != null and request.phoneNumber != ''">
                    AND u.phone_number LIKE CONCAT('%', #{request.phoneNumber}, '%')
                </if>
                <if test="request.companyName != null and request.companyName != ''">
                    AND c.name LIKE CONCAT('%', #{request.companyName}, '%')
                </if>
                <if test="request.companyUuid != null and request.companyUuid != ''">
                    AND c.uuid = #{request.companyUuid}
                </if>
                AND u.is_deleted = 0
            </where>
            </script>
            """)
    long countUsers(@Param("request") RootUserSearchRequest request);
}
