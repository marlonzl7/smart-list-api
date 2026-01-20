package com.smartlist.api.shoppinglist.controller;

import com.smartlist.api.shared.dto.ApiResponse;
import com.smartlist.api.shoppinglist.dto.FinalizePurchaseRequest;
import com.smartlist.api.shoppinglist.dto.ShoppingListResponse;
import com.smartlist.api.shoppinglist.dto.ShoppingListItemUpdateRequest;
import com.smartlist.api.shoppinglist.service.ShoppingListApplicationService;
import com.smartlist.api.shoppinglist.service.ShoppingListService;
import com.smartlist.api.user.model.User;
import com.smartlist.api.userdetails.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shopping-lists")
public class ShoppingListController {
    private final ShoppingListService shoppingListService;
    private final ShoppingListApplicationService shoppingListApplicationService;

    public ShoppingListController(ShoppingListService shoppingListService, ShoppingListApplicationService shoppingListApplicationService) {
        this.shoppingListService = shoppingListService;
        this.shoppingListApplicationService = shoppingListApplicationService;
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<ShoppingListResponse>> getActiveShoppingList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        ShoppingListResponse shoppingListDTO = shoppingListApplicationService.getActive(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lista ativa obtida com sucesso.", shoppingListDTO));
    }

    @GetMapping("/{shoppingListId}")
    public ResponseEntity<ApiResponse<ShoppingListResponse>> getShoppingListWithItems(@PathVariable Long shoppingListId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ShoppingListResponse shoppingListDTO = shoppingListService.getShoppingListWithItems(shoppingListId, userDetails.getUser());
        return ResponseEntity.ok(new ApiResponse<>(true, "Lista obtida com sucesso.", shoppingListDTO));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> updateShoppingListItem(@PathVariable Long shoppingListItemId, @AuthenticationPrincipal UserDetailsImpl userDetails, @Valid @RequestBody ShoppingListItemUpdateRequest dto) {
        shoppingListService.updateShoppingListItem(shoppingListItemId, userDetails.getUser(), dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item atualizado com sucesso", null));
    }

    @DeleteMapping("/items/{shoppingListItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteShoppingListItem(@PathVariable Long shoppingListItemId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        shoppingListService.deleteShoppingListItem(shoppingListItemId, userDetails.getUser());
        return ResponseEntity.ok(new ApiResponse<>(true, "Item excluído com sucesso", null));
    }

    @PostMapping("{shoppingListId}/finalize")
    public ResponseEntity<ApiResponse<Void>> finalize(@PathVariable Long shoppingListId, @Valid @RequestBody FinalizePurchaseRequest dto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();

        shoppingListApplicationService.finalizeShoppingList(shoppingListId, dto, user);

        return ResponseEntity.ok(new ApiResponse<>(true, "Compra finalizada com sucesso", null));
    }
}
