package com.attsw.bookstore.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "category")
    @JsonIgnoreProperties("category")
    private final List<Book> books = new ArrayList<>();

    
    public Category() {
    	// Default constructor required by JPA for entity instantiation
    }

    public Long getId() { return id; }

    public void setName(String name) { this.name = name; }
    
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void addBook(Book book) {
        if (book == null) throw new IllegalArgumentException("Book must not be null");
        if (books.contains(book)) throw new IllegalArgumentException("Book already present in category");
        books.add(book);
        book.setCategory(this);
    }

    public List<Book> getBooks() { return books; }

    public void removeBook(Book book) {
        if (book == null) throw new IllegalArgumentException("Book must not be null");
        if (!books.contains(book)) throw new IllegalArgumentException("Book not found in category");
        books.remove(book);
        book.setCategory(null);
    }
}