package com.smartlist.api.inventory.item.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemUpdateRequestDTO(
        @NotNull
        Long itemId,

        @NotBlank
        String name,

        @NotNull
        @DecimalMin(value = "0.001")
        BigDecimal quantity,

        @NotBlank
        String unit,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal price,

        @NotNull
        @DecimalMin(value = "0.001")
        BigDecimal avgConsumptionValue,

        @NotBlank
        String avgConsumptionUnit,

        @NotNull
        Long categoryId
) {
}
