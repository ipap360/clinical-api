package com.team360.hms.admissions.data;

import com.team360.hms.admissions.common.utils.DateUtils;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoField;

@Slf4j
public class PopulateDBDates implements CustomTaskChange {

    @Override
    public void execute(final Database arg0) throws CustomChangeException {
        JdbcConnection dbConn = (JdbcConnection) arg0.getConnection();
        try {

            LocalDate startDate = LocalDate.of(1970, 1, 1);
            LocalDate endDate = LocalDate.of(2100, 1, 1);

            String sql = "INSERT INTO DATES (ID, DAY_OF_WEEK, DAY_OF_MONTH, DAY_OF_YEAR, WEEK_OF_YEAR, MONTH_OF_YEAR, YEAR_OF) VALUES (?,?,?,?,?,?,?)";
            int x = 0;
            final int BATCH_SIZE = 100;

            PreparedStatement statement = dbConn.prepareStatement(sql);
            for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {

                statement.setDate(1, DateUtils.toSqlDate(date));
                statement.setInt(2, date.get(ChronoField.DAY_OF_WEEK));
                statement.setInt(3, date.get(ChronoField.DAY_OF_MONTH));
                statement.setInt(4, date.get(ChronoField.DAY_OF_YEAR));
                statement.setInt(5, date.get(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR));
                statement.setInt(6, date.get(ChronoField.MONTH_OF_YEAR));
                statement.setInt(7, date.get(ChronoField.YEAR));

                statement.addBatch();

                x++;
                if (x > BATCH_SIZE) {
                    log.debug(statement.toString());
                    statement.executeBatch();
                    x = 0;
                }
            }

            if (x != 0) {
                statement.executeBatch();
            }

        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return null;
    }

    @Override
    public void setUp() throws SetupException {

    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {

    }

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }
}
