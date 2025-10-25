package com.attsw.bookstore.web;
import java.util.Map;       
import java.util.HashMap;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import com.attsw.bookstore.model.Category;
import com.attsw.bookstore.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryRestController {

    private final CategoryService categoryService;

    public CategoryRestController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<Category> all() {
        return categoryService.getAllCategories();
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category create(@RequestBody Category category) {
        return categoryService.saveCategory(category);
    }
    
    @GetMapping("/{id}")
    public Category one(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Category not found with id: " + id);
        }
        return category;
    }
    
    
    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        return categoryService.saveCategory(category);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {    
        if (categoryService.hasBooks(id)) {
            Map<String, String> body = new HashMap<>();
            body.put("message", "Category cannot be deleted while books exist");
            return ResponseEntity.badRequest().body(body);
    }
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
}
}
