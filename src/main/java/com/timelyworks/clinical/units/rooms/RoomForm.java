package com.timelyworks.clinical.units.rooms;

import com.timelyworks.clinical.common.exceptions.FormValidationException;
import lombok.Data;

import java.util.HashMap;

@Data
public class RoomForm {

    private Integer id;

    private String name;

    private Integer capacity;

    RoomForm validate() {
        HashMap<String, String> errors = new HashMap();
        if (getName() == null) {
            errors.put("name", "Please fill the room name");
        }
        if (getCapacity() <= 0) {
            errors.put("capacity", "The room capacity should be higher than zero");
        }
        if (!errors.isEmpty()) {
            throw new FormValidationException(errors);
        }
        return this;
    }

    RoomForm load(Room room) {
        setId(room.getId());
        setName(room.getName());
        setCapacity(room.getCapacity());
        return this;
    }

}
