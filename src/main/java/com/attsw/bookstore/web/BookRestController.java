package com.attsw.bookstore.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import com.attsw.bookstore.model.Book;
import com.attsw.bookstore.service.BookService;

/**
 * REST controller for book resources.
 */
@RestController
@RequestMapping("/api/books")
public class BookRestController {

    private final BookService bookService;

    public BookRestController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> all() {
        return bookService.getAllBooks();
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(@RequestBody Book book) {
        return bookService.saveBook(book);
    }
    
    @GetMapping("/{id}")
    public Book one(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        if (book == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Book not found with id: " + id);
        }
        return book;
    }
    
    @PutMapping("/{id}")
    public Book update(@PathVariable Long id, @RequestBody Book newBook) {
        return bookService.updateBook(id, newBook);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}