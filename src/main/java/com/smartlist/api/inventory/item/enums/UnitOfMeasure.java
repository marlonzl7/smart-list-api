package com.smartlist.api.inventory.item.enums;

public enum UnitOfMeasure {
    UNIT("unidade"),
    KG("quilograma"),
    G("grama"),
    LITER("litro"),
    ML("mililitro"),
    BOX("caixa"),
    PACK("pacote"),
    DOZEN("dúzia");

    private final String label;

    UnitOfMeasure(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static boolean isValid(String value) {
        for (UnitOfMeasure unit : UnitOfMeasure.values()) {
            if (unit.getLabel().equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }

    public static UnitOfMeasure fromLabel(String label) {
        for (UnitOfMeasure unit : UnitOfMeasure.values()) {
            if (unit.getLabel().equalsIgnoreCase(label)) {
                return unit;
            }
        }

        throw new IllegalArgumentException("Unidade inválida: " + label);
    }
}
