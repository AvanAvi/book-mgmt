package com.attsw.bookstore.web;

import org.springframework.web.bind.annotation.PathVariable;
import com.attsw.bookstore.model.Book;
import com.attsw.bookstore.service.BookService;
import com.attsw.bookstore.service.CategoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web controller for book-related Thymeleaf views.
 */
@Controller
public class BookstoreWebController {

    private static final String REDIRECT_BOOKS = "redirect:/books";

    private final BookService bookService;
    private final CategoryService categoryService;

    public BookstoreWebController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/books")
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        return "books/list";
    }
    
    @GetMapping("/books/new")
    public String newBook(Model model) {
        model.addAttribute("book", Book.withTitle(""));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "books/new";
    }
    
    @PostMapping("/books")
    public String saveBook(Book book) {
        bookService.saveBook(book);
        return REDIRECT_BOOKS;
    }
    
    @GetMapping("/books/{id}/edit")
    public String editBook(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "books/edit";
    }
    
    @PostMapping("/books/{id}")
    public String updateBook(@PathVariable Long id, Book book) {
        bookService.updateBook(id, book);
        return REDIRECT_BOOKS;
    }
    
    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return REDIRECT_BOOKS;
    }
}