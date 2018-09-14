package com.team360.hms.admissions.units.patients;

import lombok.Data;

@Data
public class PatientForm {

    private String name;

    private String code;

    private String notes;

    private Integer birthYear;

    private Gender gender;

    PatientForm load(Patient patient) {

        setName(patient.getName());
        setCode(patient.getCode());
        setNotes(patient.getNotes());
        setGender(patient.getGender());
        setBirthYear(patient.getBirthYear());

        return this;
    }

}
