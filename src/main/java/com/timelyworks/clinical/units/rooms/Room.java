package com.timelyworks.clinical.units.rooms;

import com.timelyworks.clinical.common.GenericEntity;
import com.timelyworks.clinical.db.DBEntityField;
import com.timelyworks.clinical.db.DBEntityMeta;
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
