package com.smartlist.api.inventory.item.dto;

import com.smartlist.api.inventory.item.enums.AverageConsumptionUnit;
import com.smartlist.api.inventory.item.enums.UnitOfMeasure;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemUpdateRequestDTO(
        @NotBlank
        String name,

        @NotNull
        @DecimalMin(value = "0.001")
        BigDecimal quantity,

        @NotNull
        UnitOfMeasure unit,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal price,

        @NotNull
        @DecimalMin(value = "0.001")
        BigDecimal avgConsumptionValue,

        @NotNull
        AverageConsumptionUnit avgConsumptionUnit,

        @NotNull
        @Min(0)
        Integer criticalQuantityDaysOverride,

        @NotNull
        Long categoryId
) {
}
