package com.team360.hms.admissions.common;

import com.team360.hms.admissions.db.DBEntity;
import com.team360.hms.admissions.db.DBEntityField;
import com.team360.hms.admissions.db.DBUser;
import lombok.Data;

import java.time.Instant;

@Data
public abstract class GenericEntity implements DBEntity {

    @DBEntityField(name = "ID")
    public Integer id = 0;

    @DBEntityField(name = "CREATE_USER_ID")
    private Integer createUserId;

    @DBEntityField(name = "MODIFY_USER_ID")
    private Integer modifyUserId;

    @DBEntityField(name = "CREATED_AT")
    private Instant createdAt;

    @DBEntityField(name = "MODIFIED_AT")
    private Instant modifiedAt;

    @Override
    public Integer getId() {
        return id;
    }

    public DBEntity setId(Integer id) {
        this.id = id;
        return this;
    }

    public DBEntity initialize(Integer id, DBUser user) {
        setId(id);
        createUserId = user.getId();
        modifyUserId = user.getId();
        createdAt = Instant.now();
        modifiedAt = Instant.now();
        return this;
    }

    public DBEntity markModified(DBUser user) {
        modifyUserId = user.getId();
        modifiedAt = Instant.now();
        return this;
    }

}
