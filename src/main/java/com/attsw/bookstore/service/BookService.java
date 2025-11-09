package com.attsw.bookstore.service;

import java.util.List;
import com.attsw.bookstore.model.Book;

/**
 * Service layer contract for Book operations.
 */
public interface BookService {
    List<Book> getAllBooks();
    Book getBookById(Long id);
    Book saveBook(Book book);
    Book updateBook(Long id, Book book);
    void deleteBook(Long id);
    List<Book> getUncategorizedBooks();
}