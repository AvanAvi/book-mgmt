package com.attsw.bookstore.web;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import com.attsw.bookstore.service.CategoryService;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import com.attsw.bookstore.model.Book;
import com.attsw.bookstore.service.BookService;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class BookstoreWebControllerTest {

    @Mock
    private BookService bookService;
    
    @Mock
    private CategoryService categoryService;

    @Mock
    private Model model;

    @InjectMocks
    private BookstoreWebController controller;

    @Test
    void shouldReturnHomeView() {
        String view = controller.home();
        assertEquals("index", view);
    }

    @Test
    void shouldReturnListBooksView() {
        Book book = Book.withTitle("Clean Code");
        book.setAuthor("Robert Martin");
        book.setIsbn("9780132350884");
        book.setPublishedDate(LocalDate.of(2008, 8, 1));
        book.setAvailable(true);
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(book));

        String view = controller.listBooks(model);

        assertEquals("books/list", view);
        verify(model).addAttribute(eq("books"), anyList());
        verify(bookService).getAllBooks();
    }

    @Test
    void shouldReturnNewBookView() {
        when(categoryService.getAllCategories()).thenReturn(Collections.emptyList());
        
        String view = controller.newBook(model);

        assertEquals("books/new", view);
        verify(model).addAttribute(eq("book"), any(Book.class));
        verify(model).addAttribute(eq("categories"), anyList());
        verify(categoryService).getAllCategories();
    }

    @Test
    void shouldSaveBookAndRedirect() {
        Book book = Book.withTitle("TDD");
        book.setAuthor("Kent Beck");
        book.setIsbn("0321146530");
        book.setPublishedDate(LocalDate.of(2002, 11, 18));
        book.setAvailable(true);
        String redirect = controller.saveBook(book);

        assertEquals("redirect:/books", redirect);
        verify(bookService).saveBook(book);
    }

    @Test
    void shouldReturnEditBookView() {
        Book book = Book.withTitle("Clean Code");
        book.setId(1L);
        book.setAuthor("Robert Martin");
        book.setIsbn("9780132350884");
        book.setPublishedDate(LocalDate.of(2008, 8, 1));
        book.setAvailable(true);
        when(bookService.getBookById(1L)).thenReturn(book);
        when(categoryService.getAllCategories()).thenReturn(Collections.emptyList());

        String view = controller.editBook(1L, model);

        assertEquals("books/edit", view);
        verify(model).addAttribute("book", book);
        verify(model).addAttribute(eq("categories"), anyList());
        verify(bookService).getBookById(1L);
        verify(categoryService).getAllCategories();
    }

    @Test
    void shouldUpdateBookAndRedirect() {
        Book book = Book.withTitle("Updated");
        book.setAuthor("Updated Author");
        book.setIsbn("9999999999");
        book.setPublishedDate(LocalDate.of(2024, 12, 31));
        book.setAvailable(true);
        String redirect = controller.updateBook(1L, book);

        assertEquals("redirect:/books", redirect);
        verify(bookService).updateBook(1L, book);
    }

    @Test
    void shouldDeleteBookAndRedirect() {
        String redirect = controller.deleteBook(1L);

        assertEquals("redirect:/books", redirect);
        verify(bookService).deleteBook(1L);
    }
}
