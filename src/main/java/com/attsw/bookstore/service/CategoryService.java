package com.attsw.bookstore.service;

import java.util.List;
import com.attsw.bookstore.model.Category;

/**
 * Service layer contract for Category operations.
 */
public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(Long id);
    Category saveCategory(Category category);
    void deleteCategory(Long id);
    
    /**
     * Returns true if the category contains at least one book.
     */
    boolean hasBooks(Long id);
}