package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tech.cspioneer.backend.entity.User;

import java.util.List;

@Mapper
public interface CompanyUserSearchMapper {

    @Select("""
            <script>
            SELECT * FROM `user`
            <where>
                company_id = #{companyId}
                <if test="uuid != null and uuid != ''">
                    AND uuid = #{uuid}
                </if>
                <if test="username != null and username != ''">
                    AND name LIKE CONCAT('%', #{username}, '%')
                </if>
                <if test="status != null and status != ''">
                    AND status = #{status}
                </if>
                <if test="phoneNumber != null and phoneNumber != ''">
                    AND phone_number = #{phoneNumber}
                </if>
            </where>
            ORDER BY updated_at DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<User> searchCompanyUsers(@Param("companyId") Long companyId,
                                  @Param("uuid") String uuid,
                                  @Param("username") String username,
                                  @Param("status") String status,
                                  @Param("phoneNumber") String phoneNumber,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    @Select("""
            <script>
            SELECT count(*) FROM `user`
            <where>
                company_id = #{companyId}
                <if test="uuid != null and uuid != ''">
                    AND uuid = #{uuid}
                </if>
                <if test="username != null and username != ''">
                    AND name LIKE CONCAT('%', #{username}, '%')
                </if>
                <if test="status != null and status != ''">
                    AND status = #{status}
                </if>
                <if test="phoneNumber != null and phoneNumber != ''">
                    AND phone_number = #{phoneNumber}
                </if>
            </where>
            </script>
            """)
    long countSearchCompanyUsers(@Param("companyId") Long companyId,
                                 @Param("uuid") String uuid,
                                 @Param("username") String username,
                                 @Param("status") String status,
                                 @Param("phoneNumber") String phoneNumber);
}
