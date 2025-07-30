package com.smartlist.api.inventory.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRegisterRequestDTO(
        @NotBlank
        String name
) {
}
