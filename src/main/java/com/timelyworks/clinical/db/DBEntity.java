package com.timelyworks.clinical.db;

import com.timelyworks.clinical.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

public interface DBEntity {

    AesGcmEncryption AES_GCM = new AesGcmEncryption();

    default DBTable getTable() {
        DBEntityMeta meta = this.getClass().getAnnotation(DBEntityMeta.class);
        if (meta == null) throw new RuntimeException();
        return DBSchema.getInstance().getTable(meta.name());
    }

    default String getDisplayName() {
        DBEntityMeta meta = this.getClass().getAnnotation(DBEntityMeta.class);
        if (meta == null) throw new RuntimeException();
        return meta.label();
    }

    default DBEntity load(Map map) {

        Class<?> clazz = this.getClass();
        Class<?> parent = clazz.getSuperclass();

        Field[] f1 = clazz.getDeclaredFields();
        Field[] f2 = (parent != null) ? parent.getDeclaredFields() : new Field[]{};

        Field[] fields = Stream.of(f1, f2).flatMap(Stream::of).toArray(Field[]::new);

        Arrays.stream(fields).forEach(field -> {
            if (field.isAnnotationPresent(DBEntityField.class)) {
                DBEntityField F = field.getAnnotation(DBEntityField.class);
                try {
                    field.setAccessible(true);
                    boolean isEnum = field.getType().isEnum();
                    Object value = map.get(F.name());
                    if (isEnum && value != null) {
                        field.set(this, Enum.valueOf(field.getType().asSubclass(Enum.class), (String) value));
                    } else {
                        field.set(this, value);
                    }

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        return this;
    }

    default Map<String, Object> toMap() {

        Map<String, Object> map = new HashMap();
        Class<?> clazz = this.getClass();
        Class<?> parent = clazz.getSuperclass();

        Field[] f1 = clazz.getDeclaredFields();
        Field[] f2 = (parent != null) ? parent.getDeclaredFields() : new Field[]{};

        Field[] fields = Stream.of(f1, f2).flatMap(Stream::of).toArray(Field[]::new);

        String encKey = DB.getInstance().getConfig().getEnc();
        List<String> encrypted = DB.getInstance().getConfig().getEncrypted();

        Arrays.stream(fields).forEach(field -> {
            if (field.isAnnotationPresent(DBEntityField.class)) {
                try {
                    DBEntityField F = field.getAnnotation(DBEntityField.class);
                    field.setAccessible(true);
                    Object value = field.get(this);
                    if (value instanceof String) {
                        String fullName = getTable().getName() + "." + F.name().toUpperCase();
                        boolean isEncrypted = encrypted.contains(fullName);
                        if (isEncrypted && encKey != null) {
                            try {
                                map.put(F.name(), Base64.getEncoder().encodeToString(AES_GCM.encrypt(encKey.getBytes(), ((String) value).getBytes("UTF-8"), null)));
                            } catch (AuthenticatedEncryptionException e) {
                                e.printStackTrace();
                            }
                        } else {
                            int maxLength = getTable().getColumn(F.name()).getSize();
                            String str = (String) value;
                            if (str.length() > maxLength) {
                                map.put(F.name(), str.substring(0, maxLength));
                            } else {
                                map.put(F.name(), str);
                            }
                        }
                    } else if (value instanceof LocalDate) {
                        map.put(F.name(), DateUtils.toSqlDate((LocalDate) value));
                    } else if (value instanceof LocalDate) {
                        map.put(F.name(), DateUtils.toSqlTime((LocalTime) value));
                    } else if (value instanceof Instant) {
                        map.put(F.name(), DateUtils.toTimestamp((Instant) value));
                    } else {
                        map.put(F.name(), value);
                    }
                } catch (Exception e) {

                }
            }
        });

        return map;
    }

    Integer getId();

    DBEntity setId(Integer id);

    DBEntity initialize(DBUser user);

    DBEntity markModified(DBUser user);

}
