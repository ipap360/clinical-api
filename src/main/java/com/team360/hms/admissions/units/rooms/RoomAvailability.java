package com.team360.hms.admissions.units.rooms;

import lombok.Data;

@Data
public class RoomAvailability {

    private Integer male;

    private Integer female;

    private String m;

    private String f;

    public RoomAvailability() {

        male = 0;
        female = 0;

        m = "";
        f = "";

    }

}
