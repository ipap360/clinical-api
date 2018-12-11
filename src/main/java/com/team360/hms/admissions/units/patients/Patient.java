package com.team360.hms.admissions.units.patients;

import com.team360.hms.admissions.common.GenericEntity;
import com.team360.hms.admissions.db.DBEntityField;
import com.team360.hms.admissions.db.DBEntityMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@DBEntityMeta(name = "PATIENTS", label = "Patient")
public class Patient extends GenericEntity {

    @DBEntityField(name = "NAME")
    private String name;

    @DBEntityField(name = "CODE")
    private String code;

    @DBEntityField(name = "NOTES")
    private String notes;

    @DBEntityField(name = "GENDER")
    private Gender gender;

    @DBEntityField(name = "BIRTH_YEAR")
    private Integer birthYear;

    public int getAge() {
        return LocalDate.now().get(ChronoField.YEAR) - birthYear;
    }

    public Patient load(PatientForm form) {

        setId(form.getId());
        setName(form.getName());
        setCode(form.getCode());
        setNotes(form.getNotes());
        setBirthYear(form.getBirthYear());
        setGender(form.getGender());

        return this;
    }

}
