package com.attsw.bookstore.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.attsw.bookstore.model.Book;
import com.attsw.bookstore.repository.BookRepository;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    @Override
    public Book getBookById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Book saveBook(Book book) {
        return repository.save(book);
    }

    @Override
    public Book updateBook(Long id, Book book) {
        Book existing = repository.findById(id).orElse(null);
        if (existing != null) {
            existing.setTitle(book.getTitle());
            existing.setAuthor(book.getAuthor());
            existing.setIsbn(book.getIsbn());
            existing.setCategory(book.getCategory());
            return repository.save(existing);
        }
        return null;
    }

    @Override
    public void deleteBook(Long id) {
        repository.deleteById(id);
    }
}