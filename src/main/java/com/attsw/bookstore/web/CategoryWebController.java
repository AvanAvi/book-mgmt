package com.attsw.bookstore.web;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.attsw.bookstore.model.Category;
import com.attsw.bookstore.service.CategoryService;
import com.attsw.bookstore.service.BookService;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Web controller for category-related Thymeleaf views.
 */
@Controller
public class CategoryWebController {

    private static final String REDIRECT_CATEGORIES = "redirect:/categories";

    private final CategoryService categoryService;
    private final BookService bookService;

    public CategoryWebController(CategoryService categoryService, BookService bookService) {
        this.categoryService = categoryService;
        this.bookService = bookService;
    }

    @GetMapping("/categories")
    public String list(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("uncategorizedBooks", bookService.getUncategorizedBooks());
        return "categories/list";
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new Category());
        return "categories/new";
    }

    @PostMapping("/categories")
    public String saveCategory(Category category) {
        categoryService.saveCategory(category);
        return REDIRECT_CATEGORIES;
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        return "categories/edit";
    }

    @PostMapping("/categories/{id}")
    public String updateCategory(@PathVariable Long id, Category category) {
        category.setId(id);
        categoryService.saveCategory(category);
        return REDIRECT_CATEGORIES;
    }
    
    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (categoryService.hasBooks(id)) {
            Category category = categoryService.getCategoryById(id);
            redirectAttributes.addFlashAttribute("error", 
                "Cannot delete category '" + category.getName() + 
                "'. It contains " + category.getBooks().size() + 
                " book(s). Please remove or recategorize them first.");
            return REDIRECT_CATEGORIES;
        }
        categoryService.deleteCategory(id);
        return REDIRECT_CATEGORIES;
    }
}