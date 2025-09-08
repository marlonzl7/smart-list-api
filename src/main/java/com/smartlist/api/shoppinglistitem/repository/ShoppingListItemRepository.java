package com.smartlist.api.shoppinglistitem.repository;

import com.smartlist.api.shoppinglistitem.dto.ShoppingListItemDTO;
import com.smartlist.api.shoppinglistitem.model.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {

    @Query("""
           SELECT new com.smartlist.api.shoppinglistitem.dto.ShoppingListItemDTO(
               sli.shoppingListItemId,
               i.name,
               sli.purchasedQuantity,
               sli.unitaryPrice,
               sli.subtotal
           )
           FROM ShoppingListItem sli
           JOIN sli.item i
           WHERE sli.shoppingList.shoppingListId = :shoppingListId
           """)
    List<ShoppingListItemDTO> findItemsByShoppingListId(@Param("shoppingListId") Long shoppingListId);
}
