package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 根据邮箱查询用户
     * @param email 用户邮箱
     * @return 用户对象，如果不存在则返回null
     */
    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);

    /**
     * 根据UUID查询用户
     * @param uuid 用户UUID
     * @return 用户对象，如果不存在则返回null
     */
    @Select("SELECT * FROM user WHERE uuid = #{uuid}")
    User findByUuid(String uuid);

    /**
     * 插入新用户
     * @param user 用户对象
     * @return 受影响的行数
     */
    @Insert("INSERT INTO user(uuid, name, email, password, phone_number, address, avatar_url, gender, company_id, role, status, description, post_count, lesson_count, meeting_count, created_at, updated_at) " +
            "VALUES(#{uuid}, #{name}, #{email}, #{password}, #{phoneNumber}, #{address}, #{avatarUrl}, #{gender}, #{companyId}, #{role}, #{status}, #{description}, #{postCount}, #{lessonCount}, #{meetingCount}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 查询所有活跃用户
     * @return 用户列表
     */
    @Select("SELECT * FROM user WHERE is_deleted = 0 AND status = 'ACTIVE'")
    List<User> findAllActiveUsers();

    /**
     * 查询所有活跃用户的ID
     * @return 用户ID列表
     */
    @Select("SELECT id FROM user WHERE is_deleted = 0 AND status = 'ACTIVE'")
    List<Long> findAllActiveUserIds();

    /**
     * 根据公司ID查询所有活跃用户的ID
     * @param companyId 公司ID
     * @return 用户ID列表
     */
    @Select("SELECT id FROM user WHERE company_id = #{companyId} AND is_deleted = 0 AND status = 'ACTIVE'")
    List<Long> findActiveUserIdsByCompanyId(@Param("companyId") Long companyId);

    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 受影响的行数
     */
    @Update("UPDATE user SET name = #{name}, phone_number = #{phoneNumber}, address = #{address}, avatar_url = #{avatarUrl}, gender = #{gender}, company_id = #{companyId}, role = #{role}, status = #{status}, description = #{description}, updated_at = #{updatedAt} WHERE uuid = #{uuid}")
    int update(User user);

    /**
     * 根据UUID删除用户 (逻辑删除)
     * @param uuid 用户UUID
     * @return 受影响的行数
     */
    @Update("UPDATE user SET is_deleted = 1, updated_at = NOW() WHERE uuid = #{uuid}")
    int deleteByUuid(String uuid);

    /**
     * 分页查询用户
     * @param offset 偏移量
     * @param limit 数量
     * @return 用户列表
     */
    @Select("SELECT * FROM user WHERE is_deleted = 0 ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<User> findUsersWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 计算用户总数
     * @return 用户总数
     */
    @Select("SELECT COUNT(*) FROM user WHERE is_deleted = 0")
    long countAllUsers();

}
