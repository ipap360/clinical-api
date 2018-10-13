package com.team360.hms.admissions.db;

import lombok.Value;
import lombok.extern.log4j.Log4j2;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

@Value
@Log4j2
public class DBTable {

    private static final String COLUMN_NAME = "COLUMN_NAME";
    private static final String COLUMN_SIZE = "COLUMN_SIZE";
    private static final String DECIMAL_DIGITS = "DECIMAL_DIGITS";
    private static final String DATA_TYPE = "DATA_TYPE";

    private static final String SELECT_TEMPLATE = "SELECT * FROM %s";
    private static final String READ_TEMPLATE = "SELECT * FROM %s WHERE ID = :ID";
    private static final String INSERT_TEMPLATE = "INSERT INTO %1$s (%2$s) VALUES (%3$s)";
    private static final String UPDATE_TEMPLATE = "UPDATE %1$s SET %2$s WHERE ID = :ID";
    private static final String DELETE_TEMPLATE = "DELETE FROM %s WHERE ID = :ID";
    private static final String ID_TEMPLATE = "SELECT MAX(ID) + 1 FROM %s";

    private String createSql;
    private String selectSql;
    private String readSql;
    private String updateSql;
    private String deleteSql;
    private String idSql;

    private String name;
    private HashMap<String, DBColumn> columns;

    DBTable(DatabaseMetaData md, String catalog, String schema, String tableName) {

        this.name = tableName;
        columns = new HashMap<>();

        try {
            ResultSet rs = md.getColumns(catalog, schema, name, "%");
            if (rs != null) {
                while (rs.next()) {
                    String columnName = rs.getString(COLUMN_NAME);

                    DBColumn column = DBColumn
                            .builder()
                            .name(columnName)
                            .type(rs.getInt(DATA_TYPE))
                            .size(rs.getInt(COLUMN_SIZE))
                            .decimals((rs.getObject(DECIMAL_DIGITS) != null) ? rs.getInt(DECIMAL_DIGITS) : 0)
                            .build();

                    columns.put(columnName, column);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<String> insertKeys = new ArrayList<>();
        ArrayList<String> insertValues = new ArrayList<>();
        ArrayList<String> updatePairs = new ArrayList<>();

        ArrayList<String> insertOnlyKeys = new ArrayList<>();

        insertOnlyKeys.add("ID");
        insertOnlyKeys.add("CREATED_AT");
        insertOnlyKeys.add("CREATOR_ID");

        columns.forEach((key, value) -> {
            insertKeys.add(key);
            insertValues.add(":" + key);

            if (!insertOnlyKeys.contains(key)) {
                updatePairs.add(key + " = :" + key);
            }
        });

        selectSql = String.format(SELECT_TEMPLATE, name);
        readSql = String.format(READ_TEMPLATE, name);

        createSql = String.format(
                INSERT_TEMPLATE,
                name,
                String.join(", ", insertKeys),
                String.join(", ", insertValues)
        );

        updateSql = String.format(
                UPDATE_TEMPLATE,
                name,
                String.join(", ", updatePairs)
        );

        deleteSql = String.format(DELETE_TEMPLATE, name);
        idSql = String.format(ID_TEMPLATE, name);

        log.debug(this.toString());
    }

    public DBColumn getColumn(String columnName) throws DBSchemaException {
        if (!columns.containsKey(columnName)) {
            String msg = String.format("Database column `%1$s` of table `%2$s` could not be found!", columnName, name);
            throw new DBSchemaException("Column not found");
        }

        return columns.get(columnName);
    }

}



