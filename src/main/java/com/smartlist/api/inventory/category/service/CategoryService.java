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
        log.info("Cadastro de categoria iniciado. UserId={}, Name={}", user.getUserId(), dto.name());

        if (categoryRepository.findByUserAndName(user, dto.name()).isPresent()) {
            log.warn(
                    "Tentativa de cadastro de categoria duplicada. UserId={}, Name={}",
                    user.getUserId(),
                    dto.name()
            );
            throw new BadRequestException("C1001", "Já tem uma categoria registrada com esse nome.");
        }

        Category category = new Category();
        category.setName(dto.name());
        category.setUser(user);

        categoryRepository.save(category);

        log.info("Categoria cadastrada com sucesso. UserId={}, CategoryId={}", user.getUserId(), category.getCategoryId());
    }

    public void update(Long categoryId, CategoryUpdateRequestDTO dto, User user) {
        log.info("Atualização de categoria iniciada. UserId={}, CategoryId={}", user.getUserId(), categoryId);

        Category category = categoryRepository.findByUserAndCategoryId(user, categoryId)
                .orElseThrow(() -> {
                    log.warn(
                            "Tentativa de atualização de categoria inexistente. UserId={}, CategoryId={}",
                            user.getUserId(),
                            categoryId
                    );
                    return new BadRequestException("C1003", "Categoria inexistente.");
                });

        category.setName(dto.name());

        categoryRepository.save(category);

        log.info("Categoria atualizada com sucesso. UserId={}, CategoryId={}", user.getUserId(), categoryId);
    }

    public void deleteById(Long categoryId, User user) {
        log.info(
                "Tentativa de exclusão de categoria iniciada. UserId={}, CategoryId={}",
                user.getUserId(),
                categoryId
        );

        Category category = categoryRepository.findByUserAndCategoryId(user, categoryId)
                .orElseThrow(() -> {
                    log.warn(
                            "Tentativa de exclusão de categoria inexistente. UserId={}, CategoryId={}",
                            user.getUserId(),
                            categoryId
                    );
                    return new BadRequestException("C1004", "Categoria inexistente.");
                });

        categoryRepository.deleteById(categoryId);

        log.info(
                "Categoria excluída com sucesso. UserId={}, CategoryId={}",
                user.getUserId(),
                categoryId
        );
    }
}
