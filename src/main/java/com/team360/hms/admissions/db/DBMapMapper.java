package com.team360.hms.admissions.db;

import com.google.common.base.CaseFormat;
import com.team360.hms.admissions.common.utils.DateUtils;
import liquibase.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.team360.hms.admissions.db.DBEntity.AES_GCM;

@Slf4j
public class DBMapMapper implements RowMapper<Map<String, Object>> {

    CaseFormat format;

    public DBMapMapper() {
        this(null);
    }

    public DBMapMapper(CaseFormat format) {
        this.format = format;
    }

    public Map<String, Object> map(ResultSet rs, StatementContext ctx) throws SQLException {
        return (Map) this.specialize(rs, ctx).map(rs, ctx);
    }

    private boolean isEncrypted(ResultSetMetaData meta, int column) throws SQLException {
        String fullName = meta.getTableName(column) + "." + meta.getColumnName(column);
        return DB.getInstance().getConfig().getEncrypted().contains(fullName.toUpperCase());
    }

    private String getName(ResultSetMetaData meta, int column) throws SQLException {
        String alias = meta.getColumnLabel(column);
        if (alias != null) {
            return alias;
        }
        return meta.getColumnName(column);
    }

    public RowMapper<Map<String, Object>> specialize(ResultSet rs, StatementContext ctx) throws SQLException {
        ResultSetMetaData m = rs.getMetaData();

        final String secret = DB.getInstance().getConfig().getEnc();
        int columnCount = m.getColumnCount();
        return (r, c) -> {
            Map<String, Object> row = new LinkedHashMap(columnCount);
            for (int i = 1; i <= columnCount; ++i) {
                int type = m.getColumnType(i);
                String name = getName(m, i);
                if (this.format != null) {
                    name = DB.format().to(this.format, name);
                }
                switch (type) {
                    case Types.VARCHAR:
                    case Types.LONGNVARCHAR:
                    case Types.LONGVARCHAR:
                    case Types.NCHAR:
                    case Types.NVARCHAR:
                    case Types.CHAR:
                    case Types.CLOB:
                    case Types.NCLOB:
                        String value = rs.getString(i);
                        if (isEncrypted(m, i) && StringUtils.isNotEmpty(value)) {
                            try {
                                value = new String(
                                        AES_GCM.decrypt(
                                                secret.getBytes(),
                                                Base64.getDecoder().decode(value),
                                                null
                                        )
                                );
                            } catch (Exception e) {
                                log.debug(e.getMessage(), e);
                            }
                        }
                        row.put(name, value);
                        break;
                    case Types.BIGINT:
                        row.put(name, rs.getLong(i));
                        break;
                    case Types.INTEGER:
                    case Types.SMALLINT:
                    case Types.TINYINT:
                        row.put(name, rs.getInt(i));
                        break;
                    case Types.DOUBLE:
                    case Types.FLOAT:
                        row.put(name, rs.getDouble(i));
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        row.put(name, rs.getBigDecimal(i));
                        break;
                    case Types.DATE:
                        row.put(name, DateUtils.toLocalDate(rs.getDate(i)));
                        break;
                    case Types.TIME:
                        row.put(name, DateUtils.toLocalTime(rs.getTime(i)));
                        break;
                    case Types.TIMESTAMP:
                        row.put(name, DateUtils.toInstant(rs.getTimestamp(i)));
                        break;
                    case Types.BIT:
                    case Types.BOOLEAN:
                        row.put(name, rs.getBoolean(i));
                        break;
                    case Types.BLOB:
                    case Types.BINARY:
                    case Types.VARBINARY:
                    case Types.LONGVARBINARY:
                        row.put(name, rs.getBytes(i));
                        break;
                    default:
                        row.put(name, rs.getObject(i));
                        break;
                }
            }
            return row;
        };
    }
}

