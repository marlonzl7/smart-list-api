package com.smartlist.api.shoppinglistitem.repository;

import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.shoppinglist.model.ShoppingList;
import com.smartlist.api.shoppinglistitem.dto.ShoppingListItemResponse;
import com.smartlist.api.shoppinglistitem.model.ShoppingListItem;
import com.smartlist.api.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {

    @Query("""
       SELECT new com.smartlist.api.shoppinglistitem.dto.ShoppingListItemResponse(
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
    List<ShoppingListItemResponse> findItemsByShoppingListId(
            @Param("shoppingListId") Long shoppingListId
    );
    boolean existsByShoppingListAndItem(ShoppingList shoppingList, Item item);
    Optional<ShoppingListItem> findByShoppingListItemIdAndShoppingList_User(Long shoppingListItemId, User user);
}
