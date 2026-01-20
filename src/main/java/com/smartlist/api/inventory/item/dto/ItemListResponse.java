package com.smartlist.api.inventory.item.dto;

import java.math.BigDecimal;

public record ItemListResponse(
        Long itemId,
        String name,
        BigDecimal quantity,
        String unit,
        BigDecimal price,
        BigDecimal avgConsumptionValue,
        String avgConsumptionUnit,
        Long categoryId,
        String categoryName
) {}
