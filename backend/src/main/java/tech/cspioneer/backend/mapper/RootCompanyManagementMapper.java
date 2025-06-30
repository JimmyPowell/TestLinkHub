package tech.cspioneer.backend.mapper;

import org.apache.ibatis.annotations.*;
import tech.cspioneer.backend.entity.Company;
import tech.cspioneer.backend.entity.dto.request.CompanySearchRequest;
import tech.cspioneer.backend.entity.dto.response.CompanyResponse;

import java.util.List;

@Mapper
public interface RootCompanyManagementMapper {

    String COMPANY_COLUMNS = "id, uuid, name, email, phone_number, company_code, status, description, created_at, updated_at";

    @Select("<script>" +
            "SELECT " + COMPANY_COLUMNS + " FROM company " +
            "<where>" +
            "is_deleted = 0 " +
            "<if test='request.uuid != null and request.uuid != \"\"'> AND uuid = #{request.uuid} </if>" +
            "<if test='request.name != null and request.name != \"\"'> AND name LIKE CONCAT('%', #{request.name}, '%') </if>" +
            "<if test='request.companyCode != null and request.companyCode != \"\"'> AND company_code = #{request.companyCode} </if>" +
            "<if test='request.status != null and request.status != \"\"'> AND status = #{request.status} </if>" +
            "</where>" +
            "ORDER BY created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<CompanyResponse> findWithFilters(@Param("request") CompanySearchRequest request, @Param("limit") int limit, @Param("offset") int offset);

    @Select("<script>" +
            "SELECT count(*) FROM company " +
            "<where>" +
            "is_deleted = 0 " +
            "<if test='request.uuid != null and request.uuid != \"\"'> AND uuid = #{request.uuid} </if>" +
            "<if test='request.name != null and request.name != \"\"'> AND name LIKE CONCAT('%', #{request.name}, '%') </if>" +
            "<if test='request.companyCode != null and request.companyCode != \"\"'> AND company_code = #{request.companyCode} </if>" +
            "<if test='request.status != null and request.status != \"\"'> AND status = #{request.status} </if>" +
            "</where>" +
            "</script>")
    long countWithFilters(@Param("request") CompanySearchRequest request);

    @Select("SELECT * FROM company WHERE uuid = #{uuid} AND is_deleted = 0")
    Company findByUuid(String uuid);

    @Select("SELECT * FROM company WHERE company_code = #{companyCode} AND is_deleted = 0")
    Company findByCompanyCode(String companyCode);

    @Insert("INSERT INTO company (uuid, name, email, phone_number, address, avatar_url, company_code, status, description) " +
            "VALUES (#{uuid}, #{name}, #{email}, #{phoneNumber}, #{address}, #{avatarUrl}, #{companyCode}, #{status}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Company company);

    @Update("<script>" +
            "UPDATE company " +
            "<set>" +
            "<if test='name != null'>name = #{name},</if>" +
            "<if test='email != null'>email = #{email},</if>" +
            "<if test='phoneNumber != null'>phone_number = #{phoneNumber},</if>" +
            "<if test='address != null'>address = #{address},</if>" +
            "<if test='avatarUrl != null'>avatar_url = #{avatarUrl},</if>" +
            "<if test='companyCode != null'>company_code = #{companyCode},</if>" +
            "<if test='status != null'>status = #{status},</if>" +
            "<if test='description != null'>description = #{description},</if>" +
            "</set>" +
            "WHERE uuid = #{uuid} AND is_deleted = 0" +
            "</script>")
    int update(Company company);

    @Update("UPDATE company SET is_deleted = 1 WHERE uuid = #{uuid}")
    int softDelete(String uuid);

    @Update("UPDATE company SET status = #{status} WHERE uuid = #{uuid} AND is_deleted = 0")
    int updateStatus(@Param("uuid") String uuid, @Param("status") String status);
}
