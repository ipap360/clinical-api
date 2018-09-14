package com.team360.hms.admissions.units.patients;

import com.team360.hms.admissions.db.DBEntityMeta;
import com.team360.hms.admissions.db.DBEntityField;
import com.team360.hms.admissions.common.GenericEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@DBEntityMeta(name = "PATIENTS", label = "Patient")
public class Patient extends GenericEntity {

    @DBEntityField(name = "NAME")
    private String name;

    @DBEntityField(name = "CODE")
    private String code;

    @DBEntityField(name = "NOTES")
    private String notes;

    @DBEntityField(name = "GENDER")
    private Gender gender = Gender.UNKNOWN;

    @DBEntityField(name = "BIRTH_YEAR")
    private Integer birthYear;

    public int getAge() {
        return LocalDate.now().get(ChronoField.YEAR) - birthYear;
    }

//    @Override
//    public Patient load(Map map) {
//        super.load(map);
//        name = (String) map.get("NAME");
//        code = (String) map.get("CODE");
//        notes = (String) map.get("NOTES");
//        birthYear = (Integer) map.get("BIRTH_YEAR");
//        gender = (Gender) map.get("GENDER");
//        return this;
//    }
//
//    @Override
//    public Map<String, ?> toMap() {
//        Map map = super.toMap();
//        map.put("NAME", name);
//        map.put("CODE", code);
//        map.put("NOTES", notes);
//        map.put("BIRTH_YEAR", birthYear);
//        map.put("GENDER", gender);
//        return map;
//    }

}
