package com.example.learningplatform.service;

import com.example.learningplatform.domain.Category;
import com.example.learningplatform.dto.request.CreateCategoryRequest;
import com.example.learningplatform.dto.request.UpdateCategoryRequest;
import com.example.learningplatform.dto.response.CategoryResponse;
import com.example.learningplatform.exception.ConflictException;
import com.example.learningplatform.mapper.CatalogMapper;
import com.example.learningplatform.repository.CategoryRepository;
import com.example.learningplatform.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    public CategoryService(CategoryRepository categoryRepository, CourseRepository courseRepository) {
        this.categoryRepository = categoryRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        String name = request.name().trim();
        ensureUniqueName(name, null);
        Category category = new Category();
        category.setName(name);
        Category saved = categoryRepository.save(category);
        log.info("Создана категория id={} name={}", saved.getId(), saved.getName());
        return CatalogMapper.toCategory(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        Category category = requireCategory(id);
        String name = request.name().trim();
        ensureUniqueName(name, id);
        category.setName(name);
        log.info("Обновлена категория id={}", id);
        return CatalogMapper.toCategory(category);
    }

    public CategoryResponse get(Long id) {
        return CatalogMapper.toCategory(requireCategory(id));
    }

    public List<CategoryResponse> list() {
        return categoryRepository.findAll().stream().map(CatalogMapper::toCategory).toList();
    }

    @Transactional
    public void delete(Long id) {
        Category category = requireCategory(id);
        if (courseRepository.existsByCategoryId(id)) {
            throw new ConflictException("Нельзя удалить категорию: к ней привязаны курсы");
        }
        categoryRepository.delete(category);
        log.info("Удалена категория id={}", id);
    }

    public Category requireCategory(Long id) {
        return Lookups.required(categoryRepository.findById(id), "Категория с id=" + id + " не найдена");
    }

    private void ensureUniqueName(String name, Long currentId) {
        categoryRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ConflictException("Категория «" + name + "» уже существует");
            }
        });
    }
}
