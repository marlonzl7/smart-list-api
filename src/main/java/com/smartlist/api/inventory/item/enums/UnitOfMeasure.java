package com.smartlist.api.inventory.item.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.smartlist.api.exceptions.BadRequestException;

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

    @JsonCreator
    public static UnitOfMeasure fromString(String value) {
        for (UnitOfMeasure unit : UnitOfMeasure.values()) {
            if (unit.getLabel().equalsIgnoreCase(value) || unit.name().equalsIgnoreCase(value)) {
                return unit;
            }
        }

        throw new BadRequestException("EUOM1001", "Unidade de medida inválida: " + value);
    }
}
