package com.team360.hms.admissions.db;

import java.lang.reflect.Field;
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
                    boolean isString = field.getType().isAssignableFrom(String.class);
                    String fullName = getTable().getName() + "." + F.name().toUpperCase();
                    boolean isEncrypted = encrypted.contains(fullName);
                    field.setAccessible(true);
                    Object value = field.get(this);
                    if (isString && value != null && isEncrypted && encKey != null) {
                        try {
                            map.put(F.name(), Base64.getEncoder().encodeToString(AES_GCM.encrypt(encKey.getBytes(), ((String) value).getBytes(), null)));
                        } catch (AuthenticatedEncryptionException e) {
                            e.printStackTrace();
                        }
                    } else {
                        map.put(F.name(), value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return map;
    }

    Integer getId();

    DBEntity setId(Integer id);

    DBEntity initialize(Integer id, DBUser user);

    DBEntity markModified(DBUser user);

}
