package com.attsw.bookstore.integration.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSelect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.attsw.bookstore.model.Book;
import com.attsw.bookstore.model.Category;
import com.attsw.bookstore.service.BookService;
import com.attsw.bookstore.service.CategoryService;
import com.attsw.bookstore.web.BookstoreWebController;

/**
 * HTMLUnit tests for BookstoreWebController.
 * Tests HTML structure and Thymeleaf templates.
 * 
 * Based on Spring Boot Unit Tests document section 4.3.2 (pages 29–38).
 */
@WebMvcTest(controllers = BookstoreWebController.class)
class BookstoreWebControllerHtmlUnitTest {

    @Autowired
    private WebClient webClient;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private CategoryService categoryService;

    /** 
     * Helper method to remove Windows carriage returns for cross-platform compatibility. 
     */
    private String removeWindowsCR(String s) {
        return s.replace("\r", "");
    }

    // ========== TEST 1: Page Title ==========
    @Test
    void testBooksListPageTitle() throws Exception {
        when(bookService.getAllBooks()).thenReturn(Collections.emptyList());

        HtmlPage page = webClient.getPage("/books");
        assertThat(page.getTitleText()).isEqualTo("Book List");
    }

    // ========== TEST 2: Empty List Message ==========
    @Test
    void testBooksListPageWithNoBooks() throws Exception {
        when(bookService.getAllBooks()).thenReturn(Collections.emptyList());

        HtmlPage page = webClient.getPage("/books");

        HtmlElement booksList = page.getHtmlElementById("booksTable");
        assertThat(booksList).isNotNull();
        assertThat(booksList.getElementsByTagName("li")).isEmpty();
    }

    // ========== TEST 3: List with Items in Table ==========
    @Test
    void testBooksListPageWithBooks_ShouldShowThemInList() throws Exception {
        Book book1 = Book.withTitle("Clean Code");
        book1.setId(1L);
        book1.setAuthor("Robert Martin");
        book1.setIsbn("9780132350884");

        Book book2 = Book.withTitle("Refactoring");
        book2.setId(2L);
        book2.setAuthor("Martin Fowler");
        book2.setIsbn("9780201485677");

        when(bookService.getAllBooks()).thenReturn(Arrays.asList(book1, book2));

        HtmlPage page = webClient.getPage("/books");

        HtmlElement booksList = page.getHtmlElementById("booksTable");
        assertThat(booksList).isNotNull();

        String pageText = removeWindowsCR(page.asNormalizedText());
        assertThat(pageText).contains("Clean Code", "Refactoring", "Robert Martin", "Martin Fowler");
        assertThat(pageText).contains("9780132350884", "9780201485677");

        HtmlAnchor editLink1 = page.getAnchorByHref("/books/1/edit");
        assertThat(editLink1).isNotNull();
        
        HtmlAnchor editLink2 = page.getAnchorByHref("/books/2/edit");
        assertThat(editLink2).isNotNull();
    }

 // ========== TEST 4: Edit Existing Item with Form ==========
    @Test
    void testEditExistingBook() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Fiction");
        
        Book existingBook = Book.withTitle("Original Title");
        existingBook.setId(1L);
        existingBook.setAuthor("Original Author");
        existingBook.setIsbn("1234567890");
        existingBook.setCategory(category);

        when(bookService.getBookById(1L)).thenReturn(existingBook);
        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(category));

        HtmlPage page = webClient.getPage("/books/1/edit");

        assertThat(page.getTitleText()).isEqualTo("Edit Book");

        HtmlForm form = page.getForms().get(0);
        assertThat(form).isNotNull();

        String pageText = removeWindowsCR(page.asNormalizedText());
        assertThat(pageText).contains("Original Title", "Original Author", "1234567890");

        // Verify form can be filled
        HtmlInput titleInput = form.getInputByName("title");
        titleInput.setValue("Modified Title");
        
        HtmlInput authorInput = form.getInputByName("author");
        authorInput.setValue("Modified Author");
        
        HtmlInput isbnInput = form.getInputByName("isbn");
        isbnInput.setValue("0987654321");
        
        // Verify the category dropdown exists with options (1 category + 1 "No Category" option = 2 total)
        HtmlSelect categorySelect = form.getSelectByName("category");
        assertThat(categorySelect).isNotNull();
        assertThat(categorySelect.getOptions().size()).isEqualTo(2);
        
        // Verify submit button exists
        assertThat(form.getButtonByName("btn_submit")).isNotNull();
    }
     

 // ========== TEST 5: Create New Item with Form ==========
    @Test
    void testCreateNewBook() throws Exception {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Fiction");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Science");

        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(category1, category2));

        HtmlPage page = webClient.getPage("/books/new");

        assertThat(page.getTitleText()).isEqualTo("New Book");

        HtmlForm form = page.getForms().get(0);
        assertThat(form).isNotNull();

        // Verify form can be filled
        HtmlInput titleInput = form.getInputByName("title");
        titleInput.setValue("New Book Title");
        
        HtmlInput authorInput = form.getInputByName("author");
        authorInput.setValue("New Author");
        
        HtmlInput isbnInput = form.getInputByName("isbn");
        isbnInput.setValue("1111111111");
        
        // Verify the category dropdown exists with options (2 categories + 1 "No Category" option = 3 total)
        HtmlSelect categorySelect = form.getSelectByName("category");
        assertThat(categorySelect).isNotNull();
        assertThat(categorySelect.getOptions().size()).isEqualTo(3);
        
        // Verify submit button exists
        assertThat(form.getButtonByName("btn_submit")).isNotNull();
    }

    // ========== TEST 6: Link to Create New Item ==========
    @Test
    void testBooksListPage_ShouldProvideALinkForCreatingNewBook() throws Exception {
        when(bookService.getAllBooks()).thenReturn(Collections.emptyList());

        HtmlPage page = webClient.getPage("/books");

        HtmlAnchor newBookLink = page.getAnchorByText("+ New Book");
        assertThat(newBookLink.getHrefAttribute()).isEqualTo("/books/new");
    }

    // ========== TEST 7: Edit Links in Table ==========
    @Test
    void testBooksListPageWithBooks_ShouldShowEditLinks() throws Exception {
        Book book1 = Book.withTitle("Book 1");
        book1.setId(1L);

        Book book2 = Book.withTitle("Book 2");
        book2.setId(2L);

        when(bookService.getAllBooks()).thenReturn(Arrays.asList(book1, book2));

        HtmlPage page = webClient.getPage("/books");

        HtmlElement booksList = page.getHtmlElementById("booksTable");
        assertThat(booksList).isNotNull();

        String listText = removeWindowsCR(booksList.asNormalizedText());
        assertThat(listText).contains("Book 1", "Book 2", "Edit");

        HtmlAnchor editLink1 = page.getAnchorByHref("/books/1/edit");
        assertThat(editLink1).isNotNull();
        
        HtmlAnchor editLink2 = page.getAnchorByHref("/books/2/edit");
        assertThat(editLink2).isNotNull();
    }

 // ========== BONUS TEST: Verify Form Structure ==========
    @Test
    void testNewBookFormHasAllRequiredFields() throws Exception {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Fiction");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Science");

        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(category1, category2));

        HtmlPage page = webClient.getPage("/books/new");

        HtmlForm form = page.getForms().get(0);

        HtmlInput titleInput = form.getInputByName("title");
        assertThat(titleInput).isNotNull();
        
        HtmlInput authorInput = form.getInputByName("author");
        assertThat(authorInput).isNotNull();
        
        HtmlInput isbnInput = form.getInputByName("isbn");
        assertThat(isbnInput).isNotNull();
        
        HtmlSelect categorySelect = form.getSelectByName("category");
        assertThat(categorySelect).isNotNull();
        // Verify dropdown has options (2 categories + 1 "No Category" = 3 total)
        assertThat(categorySelect.getOptions().size()).isEqualTo(3);
        
        assertThat(form.getButtonByName("btn_submit")).isNotNull();
    }

    // ========== BONUS TEST: Edit Form Structure ==========
    @Test
    void testEditBookFormHasAllRequiredFields() throws Exception {
        Book book = Book.withTitle("Test Book");
        book.setId(1L);
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");

        Category category = new Category();
        category.setId(1L);
        category.setName("Fiction");
        book.setCategory(category);

        when(bookService.getBookById(1L)).thenReturn(book);
        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(category));

        HtmlPage page = webClient.getPage("/books/1/edit");

        HtmlForm form = page.getForms().get(0);

        HtmlInput methodInput = form.getInputByName("_method");
        assertThat(methodInput).isNotNull();
        assertThat(methodInput.getValue()).isEqualTo("put");
        
        HtmlInput titleInput = form.getInputByName("title");
        assertThat(titleInput).isNotNull();
        
        HtmlInput authorInput = form.getInputByName("author");
        assertThat(authorInput).isNotNull();
        
        HtmlInput isbnInput = form.getInputByName("isbn");
        assertThat(isbnInput).isNotNull();
        
        assertThat(form.getSelectByName("category")).isNotNull();
        assertThat(form.getButtonByName("btn_submit")).isNotNull();
    }
}