package com.example.webapp_project.service;

import com.example.webapp_project.model.Category;
import com.example.webapp_project.repository.CategoryRepository;
import java.util.List;

/**
 * CategoryService（single例）
 */
public class CategoryService {

    private static final CategoryService INSTANCE = new CategoryService();
    private CategoryService() {}
    public static CategoryService getInstance() { return INSTANCE; }

    private final CategoryRepository repo = CategoryRepository.getInstance();

    public List<Category> findAll() { return repo.findAll(); }

    public Category findById(Long id) {
        Category c = repo.findById(id);
        if (c == null) throw new IllegalArgumentException("Category not found");
        return c;
    }

    public Category create(String name, String description) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Category name is required");
        if (repo.findByName(name.trim()) != null) throw new IllegalArgumentException("Category name already exists");
        return repo.save(name.trim(), description != null ? description.trim() : null);
    }

    public Category update(Long id, String name, String description) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Category name is required");
        if (repo.findById(id) == null) throw new IllegalArgumentException("Category not found");
        Category dup = repo.findByName(name.trim());
        if (dup != null && !dup.getId().equals(id)) throw new IllegalArgumentException("Category name already exists");
        return repo.update(id, name.trim(), description != null ? description.trim() : null);
    }

    public void delete(Long id) {
        if (repo.findById(id) == null) throw new IllegalArgumentException("Category not found");
        repo.delete(id);
    }
}
