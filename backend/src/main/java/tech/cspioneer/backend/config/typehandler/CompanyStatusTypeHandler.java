package tech.cspioneer.backend.config.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import tech.cspioneer.backend.entity.enums.CompanyStatus;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(CompanyStatus.class)
public class CompanyStatusTypeHandler extends BaseTypeHandler<CompanyStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, CompanyStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name().toLowerCase());
    }

    @Override
    public CompanyStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : CompanyStatus.valueOf(value.toUpperCase());
    }

    @Override
    public CompanyStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : CompanyStatus.valueOf(value.toUpperCase());
    }

    @Override
    public CompanyStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : CompanyStatus.valueOf(value.toUpperCase());
    }
}
