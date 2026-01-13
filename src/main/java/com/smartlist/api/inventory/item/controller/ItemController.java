package com.smartlist.api.inventory.item.controller;

import com.smartlist.api.infra.common.dto.PageResponse;
import com.smartlist.api.inventory.item.dto.ItemListResponseDTO;
import com.smartlist.api.inventory.item.dto.ItemRegisterRequestDTO;
import com.smartlist.api.inventory.item.dto.ItemUpdateRequestDTO;
import com.smartlist.api.inventory.item.service.ItemService;
import com.smartlist.api.shared.dto.ApiResponse;
import com.smartlist.api.user.model.User;
import com.smartlist.api.userdetails.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/inventory/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ItemListResponseDTO>>> list(@AuthenticationPrincipal UserDetailsImpl userDetails, @PageableDefault(size = 10) Pageable pageable) {
        User user = userDetails.getUser();
        Page<ItemListResponseDTO> items = itemService.list(user, pageable);

        PageResponse<ItemListResponseDTO> response = new PageResponse<>();
        response.setContent(items.getContent());
        response.setPageNumber(items.getNumber());
        response.setPageSize(items.getSize());
        response.setTotalElements(items.getTotalElements());
        response.setTotalPages(items.getTotalPages());
        response.setLast(items.isLast());

        return ResponseEntity.ok(new ApiResponse<>(true, "Itens listados com sucesso.", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody @Valid ItemRegisterRequestDTO requestDTO, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        itemService.register(requestDTO, user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item cadastrado com sucesso.", null));
    }

    @PatchMapping("{itemId}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long itemId, @RequestBody @Valid ItemUpdateRequestDTO requestDTO, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        itemService.update(itemId, requestDTO, user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item atualizado com sucesso", null));
    }

    @DeleteMapping("{itemId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long itemId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        itemService.deleteById(itemId, user);
        return ResponseEntity.ok(new ApiResponse<>(true, "Item excluído com sucesso.", null));
    }
}
