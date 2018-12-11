package com.team360.hms.admissions.units.rooms;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.team360.hms.admissions.units.rooms.RoomsEndpoint.freePerGender;
import static com.team360.hms.admissions.units.rooms.RoomsEndpoint.listSubSums;
import static org.junit.Assert.*;

public class RoomsEndpointTest {

    @Test
    public void test567() {

        List<Integer> rooms = new ArrayList();

        rooms.add(7);
        rooms.add(6);
        rooms.add(5);

        int total = rooms.stream().mapToInt(Integer::intValue).sum();
        List<Integer> sub = listSubSums(rooms);
        int[] freeF = new int[total+1];
        for (int i = 0; i < total + 1; i++) {
            for (int j = i; j < total + 1; j++) {
                int[] free = freePerGender(sub, total, i, j);
                freeF[j] = free[1];
//                System.out.println("Free roooms if M:" + i + "and F:" + j + " = M:" + free[0] + ",F:" + free[1]);
                if (j > 0) {
                    assertTrue(freeF[j] < freeF[j - 1] || (freeF[j] == 0 && freeF[j-1] == 0));
                }
            }
        }
    }

}