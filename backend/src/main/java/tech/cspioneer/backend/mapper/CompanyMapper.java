package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.Company;
import java.util.List;

@Mapper
public interface CompanyMapper {

    /**
     * 根据公司代码查询公司
     * @param companyCode 公司代码
     * @return 公司对象，如果不存在则返回null
     */
    @Select("SELECT * FROM company WHERE company_code = #{companyCode}")
    Company findByCompanyCode(String companyCode);

    /**
     * 根据公司id查询公司
     * @param companyId 公司id
     * @return 公司对象，如果不存在则返回null
     */
    @Select("SELECT * FROM company WHERE id = #{companyId}")
    Company findByid(Long companyId);
    /**
     * 根据公司代码查询公司
     * @param uuid 唯一id
     * @return 公司对象，如果不存在则返回null
     */
    @Select("SELECT * FROM company WHERE uuid = #{uuid}")
    Company findByUuid(String uuid);

    /**
     * 根据公司代码查询公司
     * @param id 唯一id
     * @return 公司对象，如果不存在则返回null
     */
    @Select("SELECT * FROM company WHERE id = #{id}")
    Company findById(Long id);

    /**
     * 根据邮箱查询公司
     * @param email 公司邮箱
     * @return 公司对象，如果不存在则返回null
     */
    @Select("SELECT * FROM company WHERE email = #{email}")
    Company findByEmail(String email);
    
    /**
     * 插入新公司
     * @param company 公司对象
     * @return 受影响的行数
     */
    @Insert("INSERT INTO company(uuid, name, email, password, phone_number, address, avatar_url, company_code, status, description, created_at, updated_at) " +
            "VALUES(#{uuid}, #{name}, #{email}, #{password}, #{phoneNumber}, #{address}, #{avatarUrl}, #{companyCode}, #{status}, #{description}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Company company);

    /**
     * 物理删除公司（测试专用）
     * @param email 公司邮箱
     * @return 受影响的行数
     */
    @Delete("DELETE FROM company WHERE email = #{email}")
    int deleteByEmail(String email);

    /**
     * 根据公司代码删除公司
     * @param companyCode 公司代码
     * @return 受影响的行数
     */
    @Delete("DELETE FROM company WHERE company_code = #{companyCode}")
    int deleteByCompanyCode(@Param("companyCode") String companyCode);

    /**
     * 删除所有公司数据（测试用）
     */
    @Delete("DELETE FROM company")
    void deleteAll();

    /**
     * 重置company表自增主键（测试用）
     */
    @Update("ALTER TABLE company AUTO_INCREMENT = 1")
    void resetAutoIncrement();

}
