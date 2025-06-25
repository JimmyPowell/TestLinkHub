package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.Company;

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
     * 根据公司代码查询公司
     * @param uuid 唯一id
     * @return 公司对象，如果不存在则返回null
     */
    @Select("SELECT * FROM company WHERE uuid = #{uuid}")
    Company findByUuid(String uuid);

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
}
