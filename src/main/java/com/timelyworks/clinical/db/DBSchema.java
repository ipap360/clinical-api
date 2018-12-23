package com.timelyworks.clinical.db;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashMap;

@Log4j2
public class DBSchema {

    private static final String TABLE_NAME = "TABLE_NAME";

    @Getter
    private static DBSchema instance;
    private HashMap<String, DBTable> tables;

    private DBSchema(DatabaseMetaData md, String catalog, String schema) {
        load(md, catalog, schema);
    }

    public static void init(DatabaseMetaData md, String catalog, String schema) {
        instance = new DBSchema(md, catalog, schema);
    }

    public void reload(DatabaseMetaData md, String catalog, String schema) {
        load(md, catalog, schema);
    }

    private void load(DatabaseMetaData md, String catalog, String schema) {
        try {
            tables = new HashMap<>();
            ResultSet rs = md.getTables(catalog, schema, "%", new String[]{"TABLE"});
            if (rs != null) {
                while (rs.next()) {

                    String tableName = rs.getString(TABLE_NAME);
                    DBTable table = new DBTable(md, catalog, schema, tableName);

                    tables.put(tableName, table);
                }
            }

        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    public DBTable getTable(String tableName) throws DBSchemaException {
        if (!tables.containsKey(tableName)) {
            String msg = String.format("Database table `%s` could not be found!", tableName);
            throw new DBSchemaException(msg);
        }

        return tables.get(tableName);
    }

}
