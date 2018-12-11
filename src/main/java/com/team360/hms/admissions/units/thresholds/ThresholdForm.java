package com.team360.hms.admissions.units.thresholds;

import com.team360.hms.admissions.common.exceptions.FormValidationException;
import lombok.Data;

import java.util.HashMap;

@Data
public class ThresholdForm {

    private Integer id;

    private String description;

    private Integer threshold;

    private String indicator;

    ThresholdForm validate() {
        HashMap<String, String> errors = new HashMap();
        if (getDescription() == null) {
            errors.put("description", "Please fill the description");
        }
        if (getIndicator() == null) {
            errors.put("indicator", "Please select a visual indicator");
        }
        if (getThreshold() == null) {
            setThreshold(0);
        }
        if (!errors.isEmpty()) {
            throw new FormValidationException(errors);
        }
        return this;
    }

    ThresholdForm load(Threshold threshold) {
        setId(threshold.getId());
        setDescription(threshold.getDescription());
        setThreshold(threshold.getThreshold());
        setIndicator(threshold.getIndicator());
        return this;
    }

}
