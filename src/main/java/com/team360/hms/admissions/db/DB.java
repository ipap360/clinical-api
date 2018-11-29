package com.team360.hms.admissions.db;

import com.google.common.base.CaseFormat;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.guava.GuavaPlugin;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;

@Slf4j
public final class DB {

    @Getter
    private static DB instance;

    private HikariDataSource ds;

    private Jdbi jdbi;

    @Getter
    private DBManagerConfig config;

    private DB(DBManagerConfig config) throws SQLException, LiquibaseException {

        this.config = config;

        // data source
        initDataSource();

        // update com.team360.hms.db schema
        applyDBChanges();

        // jdbi configuration
        initJDBI();

    }

    public static Jdbi get() {
        return instance.jdbi;
    }

    public static CaseFormat format() {
        return instance.config.getFormat();
    }

    public static void start(DBManagerConfig config) throws SQLException, LiquibaseException {
        instance = new DB(config);
    }

    public static void stop() {
        instance.destroy();
        instance = null;
    }

    private void destroy() {

        jdbi = null;

        if (ds != null) {
            ds.close();
        }

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        Driver driver;

        // clear drivers
        while (drivers.hasMoreElements()) {
            try {
                driver = drivers.nextElement();
                log.debug("deregistering... " + driver.toString());
                DriverManager.deregisterDriver(driver);
            } catch (SQLException ex) {
                log.debug(ex.toString());
            } catch (Exception ex) {
                log.debug(ex.toString());
            }
        }

        // MySQL driver leaves around a thread. This static method cleans it up.
/*        try {
            AbandonedConnectionCleanupThread.shutdown();
        } catch (InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        }*/

    }

    private void initDataSource() throws SQLException {

        HikariConfig h = new HikariConfig(config.getProperties());

        h.setJdbcUrl(config.getUrl());
        h.setDriverClassName(config.getDriver());
        h.setUsername(config.getUser());
        h.setPassword(config.getPass());

        ds = new HikariDataSource(h);

    }

    private void applyDBChanges() throws SQLException, LiquibaseException {

        if (ds == null || config.getMigrations() == null) {
            return;
        }

        try (final Connection c = ds.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
            Liquibase liquibase = new liquibase.Liquibase(config.getMigrations(),
                    new ClassLoaderResourceAccessor(),
                    database);

            liquibase.update(new Contexts(), new LabelExpression());

            // liquibase disables autocommit...
            c.setAutoCommit(true);

            DBSchema.init(c.getMetaData(), c.getCatalog(), c.getSchema());

        }
    }

    private void initJDBI() {

        if (ds == null) {
            return;
        }

        jdbi = Jdbi.create(ds);

        jdbi.installPlugin(new GuavaPlugin());
//        jdbi.installPlugin(new SqlObjectPlugin());

        jdbi.setSqlLogger(new SqlLogger() {

            @Override
            public void logBeforeExecution(StatementContext context) {
//                log.debug("query: {}, params: {}", context.getRawSql(), context.getBinding().toString());
            }

            @Override
            public void logAfterExecution(StatementContext context) {
                log.debug("query: {}, params: {}, took {}ms", context.getRawSql(), context.getBinding().toString(), context.getElapsedTime(ChronoUnit.MILLIS));
            }

            @Override
            public void logException(StatementContext context, SQLException ex) {
                log.error(context.getStatement().toString(), ex);
            }

        });

    }

}
