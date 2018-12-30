package com.timelyworks.clinical.db;

import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Update;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

@Log4j2
@ToString
public class DBUtils {

    private DBUser user;

    public DBUtils(Integer userId) {
        this.user = new DBUser(userId);
    }

    public void create(DBEntity... entities) {
        try {
            DB.get().useTransaction(TransactionIsolationLevel.SERIALIZABLE, db -> {
                Arrays.stream(entities).forEach(entity -> {
                    Integer id = _insert(db, entity);
                    if (id == null) {
                        throw new DBOperationException("Failed to insert " + entity.getDisplayName());
                    }
                });
            });
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            throw new DBOperationException("Database operation failed");
        }
    }

    public void read(DBEntity... entities) {
        try {
            Arrays.stream(entities).forEach(entity -> {
                read(entity);
            });
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            throw new DBOperationException("Database operation failed");
        }
    }

    private void read(DBEntity entity) {
        Optional<Map<String, Object>> map = DB.get().withHandle(db -> db.createQuery(entity.getTable().getReadSql())
                .bind("ID", entity.getId())
                .map(new DBMapMapper())
                .findFirst());

        if (!map.isPresent()) {
            throw new DBOperationException("The " + entity.getDisplayName() + " you requested doesn't exist");
        }

        entity.load(map.get());
    }

    public void update(DBEntity... entities) {
        try {
            DB.get().useTransaction(TransactionIsolationLevel.SERIALIZABLE, db -> {
                Arrays.stream(entities).forEach(entity -> {
                    Integer rows = _update(db, entity);
                    if (rows != 1) {
                        throw new DBOperationException("Failed to update " + entity.getDisplayName());
                    }
                });
            });
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            throw new DBOperationException("Database operation failed");
        }
    }

    public void upsert(DBEntity... entities) {
        try {
            DB.get().useTransaction(TransactionIsolationLevel.SERIALIZABLE, db -> {
                Arrays.stream(entities).forEach(entity -> {
                    if (entity.getId() == 0) {
                        Integer id = _insert(db, entity);
                        if (id == null) {
                            throw new DBOperationException("Failed to insert " + entity.getDisplayName());
                        }
                        entity.setId(id);
                    } else {
                        Integer rows = _update(db, entity);
                        if (rows != 1) {
                            throw new DBOperationException("Failed to update " + entity.getDisplayName());
                        }
                    }
                });
            });
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            throw new DBOperationException("Database operation failed");
        }
    }

    public void delete(DBEntity... entities) {
        delete(null, entities);
    }

    public void delete(BiFunction<Handle, DBEntity, Boolean> f, DBEntity... entities) {
        try {
            DB.get().useTransaction(TransactionIsolationLevel.SERIALIZABLE, db -> {
                Arrays.stream(entities).forEach(entity -> {
                    Integer rows = _delete(db, entity);
                    Boolean ok = (f != null) ? f.apply(db, entity) : true;
                    if (rows != 1 || !ok) {
                        db.rollback();
                        throw new DBOperationException("Failed to delete " + entity.getDisplayName());
                    }
                    entity.load(new HashMap<String, Object>());
                });
            });
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            throw new DBOperationException("Database operation failed");
        }
    }

    private Integer _insert(Handle db, DBEntity entity) {

        Update upd = db.createUpdate(entity.getTable().getCreateSql())
                .bindMap(entity.initialize(user).toMap());

        Optional<Integer> id = upd
                .executeAndReturnGeneratedKeys("ID")
                .mapTo(Integer.class)
                .findFirst();

        if (id.isPresent()) {
            return id.get();
        }

        return null;
    }

    private int _update(Handle db, DBEntity entity) {
        return db.createUpdate(entity.getTable().getUpdateSql())
                .bindMap(entity.markModified(user).toMap())
                .execute();
    }

    private int _delete(Handle db, DBEntity entity) {
        return db.createUpdate(entity.getTable().getDeleteSql())
                .bind("ID", entity.getId())
                .execute();
    }

}
