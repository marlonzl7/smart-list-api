package com.smartlist.api.shoppinglist.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record FinalizePurchaseRequest(
        @NotEmpty(message = "Lista de itens não pode estar vazia")
        @Valid
        List<PurchasedItemRequest> items
) {}
