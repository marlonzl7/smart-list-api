package com.smartlist.api.inventory.item.enums;

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

    public static boolean isValid(String value) {
        for (AverageConsumptionUnit unit : AverageConsumptionUnit.values()) {
            if (unit.getLabel().equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }

    public static AverageConsumptionUnit fromLabel(String label) {
        for (AverageConsumptionUnit unit : AverageConsumptionUnit.values()) {
            if (unit.getLabel().equalsIgnoreCase(label)) {
                return unit;
            }
        }

        throw new IllegalArgumentException("Unidade inválida: " + label);
    }
}
