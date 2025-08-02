package com.smartlist.api.inventory.item.dto;

import com.smartlist.api.inventory.category.model.Category;
import com.smartlist.api.inventory.item.enums.AverageConsumptionUnit;
import com.smartlist.api.inventory.item.enums.UnitOfMeasure;
import com.smartlist.api.user.model.User;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemRegisterRequestDTO(
    Long categoryId,

    @NotBlank
    String name,

    @NotNull
    @DecimalMin(value = "0.001")
    BigDecimal quantity,

    @NotNull
    UnitOfMeasure unit,

    @NotNull
    @DecimalMin(value = "0.001")
    BigDecimal avgConsumptionValue,

    @NotNull
    AverageConsumptionUnit avgConsumptionUnit,

    @NotNull
    @DecimalMin(value = "0.00")
    BigDecimal price
) {}
