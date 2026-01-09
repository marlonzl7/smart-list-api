package com.smartlist.api.inventory.item.dto;

import com.smartlist.api.inventory.category.model.Category;
import com.smartlist.api.inventory.item.enums.AverageConsumptionUnit;
import com.smartlist.api.inventory.item.enums.UnitOfMeasure;
import com.smartlist.api.user.model.User;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemRegisterRequestDTO(
    Long categoryId,

    @NotBlank(message = "Nome é obrigatório")
    String name,

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(value = "0.001")
    BigDecimal quantity,

    @NotNull(message = "Unidade de medida é obrigatória")
    UnitOfMeasure unit,

    @NotNull(message = "Média de consumo é obrigatória")
    @DecimalMin(value = "0.001")
    BigDecimal avgConsumptionValue,

    @NotNull(message = "Unidade de medida de consumo é obrigatória")
    AverageConsumptionUnit avgConsumptionUnit,

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.00")
    BigDecimal price,

    @Min(0)
    Integer criticalQuantityDaysOverride
) {}
