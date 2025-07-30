package com.smartlist.api.inventory.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryUpdateRequestDTO(
        @NotNull
        Long categoryId,

        @NotBlank
        String name
) {
}
