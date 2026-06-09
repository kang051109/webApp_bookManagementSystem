package com.example.webapp_project.service;

import com.example.webapp_project.model.Category;
import org.springframework.stereotype.Service;;
import com.example.webapp_project.repository.CategoryRepository;
import org.springframework.stereotype.Service;;
import java.util.List;
/**
 * 分类业务服务（单例）
 */
@Service
public class CategoryService {
    private static final CategoryService INSTANCE = new CategoryService();
    private CategoryService() {}
    public static CategoryService getInstance() { return INSTANCE; }
    private final CategoryRepository repo = CategoryRepository.getInstance();
    public List<Category> findAll() { return repo.findAll(); }
    public Category findById(Long id) {
        Category c = repo.findById(id);
        if (c == null) throw new IllegalArgumentException("分类不存在");
        return c;
    }
    public Category create(String name, String description) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("分类名称不能为空");
        if (repo.findByName(name.trim()) != null) throw new IllegalArgumentException("分类名称已存在");
        return repo.save(name.trim(), description != null ? description.trim() : null);
    public Category update(Long id, String name, String description) {
        if (repo.findById(id) == null) throw new IllegalArgumentException("分类不存在");
        Category dup = repo.findByName(name.trim());
        if (dup != null && !dup.getId().equals(id)) throw new IllegalArgumentException("分类名称已存在");
        return repo.update(id, name.trim(), description != null ? description.trim() : null);
    public void delete(Long id) {
        repo.delete(id);
}
