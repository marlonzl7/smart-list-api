package com.smartlist.api.inventory.item.dto;

import com.smartlist.api.inventory.category.model.Category;

import java.math.BigDecimal;

public record ItemListResponseDTO(
        Long itemId,
        String name,
        BigDecimal quantity,
        String unit,
        BigDecimal price,
        BigDecimal avgConsumption,
        Long categoryId,
        String categoryName
) {}
