package com.smartlist.api.inventory.category.service;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.inventory.category.dto.CategoryRegisterRequestDTO;
import com.smartlist.api.inventory.category.dto.CategoryUpdateRequestDTO;
import com.smartlist.api.inventory.category.dto.CategoryListResponseDTO;
import com.smartlist.api.inventory.category.model.Category;
import com.smartlist.api.inventory.category.repository.CategoryRepository;
import com.smartlist.api.user.model.User;
import com.smartlist.api.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public CategoryService(CategoryRepository categoryRepository, UserService userService) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    public Page<CategoryListResponseDTO> list(User user, Pageable pageable) {
        return categoryRepository.findByUser(user, pageable)
                .map(category -> new CategoryListResponseDTO(
                        category.getCategoryId(),
                        category.getName()
                ));
    }

    public List<CategoryListResponseDTO> listAll(User user) {
        return categoryRepository.findByUser(user).stream()
                .map(category -> new CategoryListResponseDTO(category.getCategoryId(), category.getName()))
                .toList();
    }

    public void register(CategoryRegisterRequestDTO dto, User user) {
        log.info("Iniciando cadastro de categoria");

        if (categoryRepository.findByUserAndName(user, dto.name()).isPresent()) {
            log.error("Tentativa de cadastro de categoria já registrada.");
            throw new BadRequestException("C1001", "Já tem uma categoria registrada com esse nome.");
        }

        Category category = new Category();
        category.setName(dto.name());
        category.setUser(user);

        categoryRepository.save(category);

        log.info("Categoria cadastrada com sucesso.");
    }

    public void update(CategoryUpdateRequestDTO dto, User user) {
        log.info("Iniciando atualização de categoria");

        Category category = categoryRepository.findByUserAndCategoryId(user, dto.categoryId()).orElseThrow(() -> {
            log.error("Tentativa de atualização de categoria inexistente.");
            return new BadRequestException("C1003", "Categoria inexistente.");
        });

        category.setName(dto.name());

        categoryRepository.save(category);

        log.info("Categoria atualizada com sucesso.");
    }

    public void deleteById(Long categoryId, User user) {
        log.info("Iniciando exclusão de categoria.");

        Category category = categoryRepository.findByUserAndCategoryId(user, categoryId).orElseThrow(() -> {
            log.error("Tentativa de exclusão de categoria inexistente.");
            return new BadRequestException("C1004", "Categoria inexistente.");
        });

        categoryRepository.deleteById(categoryId);

        log.info("Categoria excluída com sucesso.");
    }
}
