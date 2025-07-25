package com.smartlist.api.inventory.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryCRUDRequestDTO(
        Long categoryId,

        @NotBlank
        String name
) {
}
