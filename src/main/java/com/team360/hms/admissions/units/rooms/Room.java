package com.team360.hms.admissions.units.rooms;

import com.team360.hms.admissions.common.GenericEntity;
import com.team360.hms.admissions.db.DBEntityField;
import com.team360.hms.admissions.db.DBEntityMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@DBEntityMeta(name = "ROOMS", label = "Room")
public class Room extends GenericEntity {

    @DBEntityField(name = "NAME")
    private String name;

    @DBEntityField(name = "CAPACITY")
    private Integer capacity;

    public Room load(RoomForm form) {
        setId(form.getId());
        setName(form.getName());
        setCapacity(form.getCapacity());
        return this;
    }

}
