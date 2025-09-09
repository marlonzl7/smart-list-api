package com.smartlist.api.inventory.item.controller;

import com.smartlist.api.infra.common.dto.PageResponse;
import com.smartlist.api.inventory.item.dto.ItemListResponseDTO;
import com.smartlist.api.inventory.item.dto.ItemRegisterRequestDTO;
import com.smartlist.api.inventory.item.dto.ItemUpdateRequestDTO;
import com.smartlist.api.inventory.item.service.ItemService;
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
@RequestMapping("/inventory")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ItemListResponseDTO>> list(@AuthenticationPrincipal UserDetailsImpl userDetails, @PageableDefault(size = 10) Pageable pageable) {
        User user = userDetails.getUser();
        Page<ItemListResponseDTO> items = itemService.list(user, pageable);

        PageResponse<ItemListResponseDTO> response = new PageResponse<>();
        response.setContent(items.getContent());
        response.setPageNumber(items.getNumber());
        response.setPageSize(items.getSize());
        response.setTotalElements(items.getTotalElements());
        response.setTotalPages(items.getTotalPages());
        response.setLast(items.isLast());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/item/register")
    public ResponseEntity<String> register(@RequestBody @Valid ItemRegisterRequestDTO requestDTO, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        itemService.register(requestDTO, user);
        return ResponseEntity.ok("Item cadastrado com sucesso.");
    }

    @PatchMapping("/item/update")
    public ResponseEntity<String> update(@RequestBody @Valid ItemUpdateRequestDTO requestDTO, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        itemService.update(requestDTO, user);
        return ResponseEntity.ok("Item atualizado com sucesso");
    }

    @DeleteMapping("/item/delete/{itemId}")
    public ResponseEntity<String> delete(@PathVariable Long itemId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        itemService.deleteById(itemId, user);
        return ResponseEntity.ok("Item excluído com sucesso.");
    }
}
