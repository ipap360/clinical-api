package com.team360.hms.units.patients;

import com.team360.hms.db.DBQuery;

public class PatientsQuery implements DBQuery {

    @Override
    public String getSql() {
        return "SELECT * FROM PATIENTS";
    }
}
