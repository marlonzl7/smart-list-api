package com.smartlist.api.shoppinglist.controller;

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
    public ResponseEntity<ShoppingListDTO> getActiveShoppingList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
            User user = userDetails.getUser();
        ShoppingListDTO shoppingListDTO = shoppingListService.getActiveShoppingListByUser(user);
        return ResponseEntity.ok(shoppingListDTO);
    }

    @GetMapping("/{shoppingListId}")
    public ResponseEntity<ShoppingListDTO> getShoppingListWithItems(@PathVariable Long shoppingListId) {
        ShoppingListDTO shoppingListDTO = shoppingListService.getShoppingListWithItems(shoppingListId);
        return ResponseEntity.ok(shoppingListDTO);
    }

    @PostMapping("/update-item")
    public ResponseEntity<String> updateShoppingListItem(@Valid @RequestBody ShoppingListItemUpdateRequest dto) {
        shoppingListService.updateShoppingListItem(dto);
        return ResponseEntity.ok("Item atualizado com sucesso");
    }

    @DeleteMapping("/delete-item/{shoppingListItemId}")
    public ResponseEntity<String> deleteShoppingListItem(@PathVariable Long shoppingListItemId) {
        shoppingListService.deleteShoppingListItemById(shoppingListItemId);
        return ResponseEntity.ok("Item excluído com sucesso");
    }
}
