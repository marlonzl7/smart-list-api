package com.smartlist.api.inventory.item.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.smartlist.api.exceptions.BadRequestException;

public enum AverageConsumptionUnit {
    DAY("dia"),
    WEEK("semana"),
    MONTH("mes");

    private final String label;

    AverageConsumptionUnit(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static AverageConsumptionUnit fromString(String value) {
        for (AverageConsumptionUnit unit : AverageConsumptionUnit.values()) {
            if (unit.getLabel().equalsIgnoreCase(value) || unit.name().equalsIgnoreCase(value)) {
                return unit;
            }
        }

        throw new BadRequestException("EACU1001", "Unidade de medida inválida: " + value);
    }
}
