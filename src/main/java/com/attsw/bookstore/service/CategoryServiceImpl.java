package com.attsw.bookstore.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.attsw.bookstore.model.Category;
import com.attsw.bookstore.repository.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Category operations.
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    public CategoryServiceImpl(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Category> getAllCategories() {
        return repository.findAll();
    }

    @Override
    public Category getCategoryById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Category saveCategory(Category category) {
        return repository.save(category);
    }

    @Override
    public void deleteCategory(Long id) {
        repository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasBooks(Long id) {
        Category category = repository.findById(id).orElse(null);
        return category != null && !category.getBooks().isEmpty();
    }
}