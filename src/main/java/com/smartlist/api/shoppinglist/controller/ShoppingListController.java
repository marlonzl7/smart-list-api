package com.smartlist.api.shoppinglist.controller;

import com.smartlist.api.shared.dto.ApiResponse;
import com.smartlist.api.shoppinglist.dto.ShoppingListDTO;
import com.smartlist.api.shoppinglist.dto.ShoppingListItemUpdateRequest;
import com.smartlist.api.shoppinglist.service.ShoppingListService;
import com.smartlist.api.user.model.User;
import com.smartlist.api.userdetails.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shopping-list")
public class ShoppingListController {
    private final ShoppingListService shoppingListService;

    public ShoppingListController(ShoppingListService shoppingListService) {
        this.shoppingListService = shoppingListService;
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<ShoppingListDTO>> getActiveShoppingList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
            User user = userDetails.getUser();
        ShoppingListDTO shoppingListDTO = shoppingListService.getActiveShoppingListByUser(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lista ativa obtida com sucesso.", shoppingListDTO));
    }

    @GetMapping("/{shoppingListId}")
    public ResponseEntity<ApiResponse<ShoppingListDTO>> getShoppingListWithItems(@PathVariable Long shoppingListId) {
        ShoppingListDTO shoppingListDTO = shoppingListService.getShoppingListWithItems(shoppingListId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lista obtida com sucesso.", shoppingListDTO));
    }

    @PatchMapping("/update-item")
    public ResponseEntity<String> updateShoppingListItem(@Valid @RequestBody ShoppingListItemUpdateRequest dto) {
        shoppingListService.updateShoppingListItem(dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item atualizado com sucesso", null));
    }

    @DeleteMapping("/delete-item/{shoppingListItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteShoppingListItem(@PathVariable Long shoppingListItemId) {
        shoppingListService.deleteShoppingListItemById(shoppingListItemId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item excluído com sucesso", null));
    }
}
