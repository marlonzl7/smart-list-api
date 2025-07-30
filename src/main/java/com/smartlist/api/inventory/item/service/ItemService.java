package com.smartlist.api.inventory.item.service;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.category.model.Category;
import com.smartlist.api.inventory.category.repository.CategoryRepository;
import com.smartlist.api.inventory.item.dto.ItemListResponseDTO;
import com.smartlist.api.inventory.item.dto.ItemRegisterRequestDTO;
import com.smartlist.api.inventory.item.dto.ItemUpdateRequestDTO;
import com.smartlist.api.inventory.item.enums.UnitOfMeasure;
import com.smartlist.api.inventory.item.model.Item;
import com.smartlist.api.inventory.item.repository.ItemRepository;
import com.smartlist.api.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ItemService {
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    public ItemService(ItemRepository itemRepository, CategoryRepository categoryRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<ItemListResponseDTO> list(User user, Pageable pageable) {
        return itemRepository.findByUser(user, pageable)
                .map(item -> new ItemListResponseDTO(
                        item.getItemId(),
                        item.getName(),
                        item.getQuantity(),
                        item.getUnit().getLabel(),
                        item.getPrice(),
                        item.getAvgConsumption(),
                        item.getCategory().getCategoryId(),
                        item.getCategory().getName()
                ));
    }

    public void register(ItemRegisterRequestDTO dto, User user) {
        log.info("Iniciando cadastro de item");

        if (!UnitOfMeasure.isValid(dto.unit())) {
            throw new BadRequestException("I1001", "Unidade de medida inválida");
        }

        Item item = new Item();
        item.setUser(user);
        item.setName(dto.name());
        item.setQuantity(dto.quantity());
        item.setAvgConsumption(dto.avgConsumption());
        item.setPrice(dto.price());
        item.setUnit(UnitOfMeasure.fromLabel(dto.unit()));

        if (dto.categoryId() != null) {

            Category category = categoryRepository.findByUserAndCategoryId(user, dto.categoryId())
                    .orElseThrow(() -> {
                        log.error("Tentativa de cadastro de item com categoria inexistente");
                        return new BadRequestException("I1002", "Categoria inexistente");
                    });

            item.setCategory(category);
        }

        itemRepository.save(item);

        log.info("Cadastro de item realizado com sucesso.");
    }

    public void update(ItemUpdateRequestDTO dto, User user) {
        log.info("Iniciando atualização de Item");

        Item item = itemRepository.findByUserAndItemId(user, dto.itemId()).orElseThrow(() -> {
            log.error("Tentativa de atualização de item inexistente");
            return new BadRequestException("I1003", "Item inexistente");
        });

        if (!dto.name().equals(item.getName())) {
            item.setName(dto.name());
        }

        if (!dto.quantity().equals(item.getQuantity())) {
            item.setQuantity(dto.quantity());
        }

        if (!dto.unit().equals(item.getUnit().getLabel())) {
            if (!UnitOfMeasure.isValid(dto.unit())) {
                throw new BadRequestException("I1001", "Unidade de medida inválida");
            }

            item.setUnit(UnitOfMeasure.fromLabel(dto.unit()));
        }

        if (!dto.price().equals(item.getPrice())) {
            item.setPrice(dto.price());
        }

        if (!dto.avgConsumption().equals(item.getAvgConsumption())) {
            item.setAvgConsumption(dto.avgConsumption());
        }

        itemRepository.save(item);

        log.info("Item atualizado com sucesso.");
    }

    public void deleteById(Long itemId, User user) {
        log.info("Iniciando exclusão de item");

        Item item = itemRepository.findByUserAndItemId(user, itemId).orElseThrow(() -> {
            log.error("Tentativa de exclusão de item inexistente");
            return new BadRequestException("I1004", "Item inexistente");
        });

        itemRepository.deleteById(itemId);

        log.info("Item excluído com sucesso.");
    }
}
