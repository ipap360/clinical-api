package com.team360.hms.admissions.units.thresholds;

import com.team360.hms.admissions.common.GenericEntity;
import com.team360.hms.admissions.db.DBEntityField;
import com.team360.hms.admissions.db.DBEntityMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@DBEntityMeta(name = "THRESHOLDS", label = "Threshold")
public class Threshold extends GenericEntity {

    @DBEntityField(name = "DESCRIPTION")
    private String description;

    @DBEntityField(name = "INDICATOR")
    private String indicator;

    @DBEntityField(name = "THRESHOLD")
    private Integer threshold;

    public Threshold load(ThresholdForm form) {
        setId(form.getId());
        setDescription(form.getDescription());
        setThreshold(form.getThreshold());
        setIndicator(form.getIndicator());
        return this;
    }

}
