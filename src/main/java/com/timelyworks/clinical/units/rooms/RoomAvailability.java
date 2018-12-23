package com.timelyworks.clinical.units.rooms;

import lombok.Data;

@Data
public class RoomAvailability {

    private Integer male = 0;
    private Integer female = 0;

    private Integer M = 0;
    private Integer F = 0;
    private Integer Total = 0;

}
