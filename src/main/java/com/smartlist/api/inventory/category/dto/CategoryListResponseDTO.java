package com.smartlist.api.inventory.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryListResponseDTO {
    private Long categoryId;
    private String name;
}
