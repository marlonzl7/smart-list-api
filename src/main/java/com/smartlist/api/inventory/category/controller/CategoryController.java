package com.smartlist.api.inventory.category.controller;

import com.smartlist.api.infra.common.dto.PageResponse;
import com.smartlist.api.inventory.category.dto.CategoryRegisterRequestDTO;
import com.smartlist.api.inventory.category.dto.CategoryUpdateRequestDTO;
import com.smartlist.api.inventory.category.dto.CategoryListResponseDTO;
import com.smartlist.api.inventory.category.service.CategoryService;
import com.smartlist.api.shared.dto.ApiResponse;
import com.smartlist.api.user.model.User;
import com.smartlist.api.userdetails.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<ApiResponse<PageResponse<CategoryListResponseDTO>>> list(@AuthenticationPrincipal UserDetailsImpl userDetails, @PageableDefault(size = 10) Pageable pageable) {
        User user = userDetails.getUser();
        Page<CategoryListResponseDTO> categories = categoryService.list(user, pageable);

        PageResponse<CategoryListResponseDTO> response = new PageResponse<>();
        response.setContent(categories.getContent());
        response.setPageNumber(categories.getNumber());
        response.setPageSize(categories.getSize());
        response.setTotalElements(categories.getTotalElements());
        response.setTotalPages(categories.getTotalPages());
        response.setLast(categories.isLast());

        return ResponseEntity.ok(new ApiResponse<>(true, "Categorias páginadas listadas com sucesso.", response));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CategoryListResponseDTO>>> listAll(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        List<CategoryListResponseDTO> categories = categoryService.listAll(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Categorias listadas com sucesso.", categories));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody @Valid CategoryRegisterRequestDTO registerRequestDTO, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        categoryService.register(registerRequestDTO, user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Categoria registrada com sucesso.", null));
    }


    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<Void>> update(@RequestBody @Valid CategoryUpdateRequestDTO updateRequestDTO, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        categoryService.update(updateRequestDTO, user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Categoria atualizada com sucesso.", null));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        categoryService.deleteById(id, user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Categoria excluída com sucesso.", null));
    }
}
