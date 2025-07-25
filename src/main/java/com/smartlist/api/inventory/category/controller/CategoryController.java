package com.smartlist.api.inventory.category.controller;

import com.smartlist.api.inventory.category.dto.CategoryCRUDRequestDTO;
import com.smartlist.api.inventory.category.dto.CategoryListResponseDTO;
import com.smartlist.api.inventory.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@RequestMapping("/inventory/category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<Page<CategoryListResponseDTO>> list(Pageable pageable) {
        Page<CategoryListResponseDTO> categories = categoryService.list(pageable);
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid CategoryCRUDRequestDTO registerRequestDTO) {
        categoryService.register(registerRequestDTO);
        return ResponseEntity.ok("Categoria registrada com sucesso.");
    }

    @PostMapping("/update")
    public ResponseEntity<String> update(@RequestBody @Valid CategoryCRUDRequestDTO updateRequestDTO) {
        categoryService.update(updateRequestDTO);
        return ResponseEntity.ok("Categoria atualizada com sucesso.");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        categoryService.deleteById(id);
        return ResponseEntity.ok("Categoria excluída com sucesso.");
    }
}
