package com.timelyworks.clinical.units.patients;

import com.timelyworks.clinical.common.exceptions.FormValidationException;
import lombok.Data;

import java.util.HashMap;
import java.util.Optional;

@Data
public class PatientForm {

    private Integer id;

    private String name;

    private String code;

    private String notes;

    private Integer birthYear;

    private Gender gender;

    PatientForm validate() {
        HashMap<String, String> errors = new HashMap();

        if (getName() == null) {
            errors.put("name", "Please fill the name");
        }

        if (getGender() == null) {
            errors.put("gender", "Please select the gender");
        }

        Optional<Integer> exists = (new PatientDao()).checkCodeExists(getId(), getCode());
        if (exists.isPresent()) {
            errors.put("code", "A patient with the same code already exists");
        }

        if (!errors.isEmpty()) {
            throw new FormValidationException(errors);
        }
        return this;
    }

    PatientForm load(Patient patient) {

        setId(patient.getId());
        setName(patient.getName());
        setCode(patient.getCode());
        setNotes(patient.getNotes());
        setGender(patient.getGender());
        setBirthYear(patient.getBirthYear());

        return this;
    }

}
