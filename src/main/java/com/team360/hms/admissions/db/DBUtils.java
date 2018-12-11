package com.team360.hms.admissions.db;

import com.team360.hms.admissions.common.utils.DateUtils;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Update;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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

    private static Map<String, Object> normalizeMap(Map<String, Object> map) {
        // Base64.getEncoder().encodeToString(AES_GCM.encrypt(encKey.getBytes(), value.getBytes(), null))
        map.entrySet().stream().forEach(e -> {
            Object v = e.getValue();
            if (v instanceof LocalDate) {
                e.setValue(DateUtils.toSqlDate((LocalDate) v));
            } else if (v instanceof LocalTime) {
                e.setValue(DateUtils.toSqlTime((LocalTime) v));
            } else if (v instanceof Instant) {
                e.setValue(DateUtils.toTimestamp((Instant) v));
            }
        });
        return map;
    }

    public void create(DBEntity... entities) {
        DB.get().useTransaction(TransactionIsolationLevel.SERIALIZABLE, db -> {
            Arrays.stream(entities).forEach(entity -> {
                Integer id = _insert(db, entity);
                if (id == null) {
                    throw new DBOperationException("Failed to insert " + entity.getDisplayName());
                }
            });
        });
    }

    public void read(DBEntity... entities) {
        Arrays.stream(entities).forEach(entity -> {
            read(entity);
        });
    }

    public void read(DBEntity entity) {

        Optional<Map<String, Object>> map = DB.get().withHandle(db -> db.createQuery(entity.getTable().getReadSql())
                .bind("ID", entity.getId())
                .map(new DBMapMapper())
                .findFirst());

        if (!map.isPresent()) {
            throw new DBOperationException("The " + entity.getDisplayName() + " you requested doesn't exist");
        }

        entity.load(map.get());
    }

    public void update(DBEntity... entities) throws DBOperationException {
        DB.get().useTransaction(TransactionIsolationLevel.SERIALIZABLE, db -> {
            Arrays.stream(entities).forEach(entity -> {
                Integer rows = _update(db, entity);
                if (rows != 1) {
                    throw new DBOperationException("Failed to update " + entity.getDisplayName());
                }
            });
        });
    }

    public void upsert(DBEntity... entities) throws DBOperationException {
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
    }

    public void delete(DBEntity... entities) throws DBOperationException {
        delete(null, entities);
    }

    public void delete(BiFunction<Handle, DBEntity, Boolean> f, DBEntity... entities) throws DBOperationException {
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
    }

    private Integer _insert(Handle db, DBEntity entity) {

//        int id = db.createQuery(entity.getTable().getIdSql())
//                .mapTo(Integer.class)
//                .findFirst()
//                .orElse(1);

        Update upd = db.createUpdate(entity.getTable().getCreateSql())
                .bindMap(normalizeMap(entity.initialize(user).toMap()));

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
                .bindMap(normalizeMap(entity.markModified(user).toMap()))
                .execute();
    }

    private int _delete(Handle db, DBEntity entity) {
        return db.createUpdate(entity.getTable().getDeleteSql())
                .bind("ID", entity.getId())
                .execute();
    }

}
