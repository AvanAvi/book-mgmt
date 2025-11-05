package com.attsw.bookstore.integration.rest;
import java.time.LocalDate;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.attsw.bookstore.model.Book;
import com.attsw.bookstore.model.Category;
import com.attsw.bookstore.repository.BookRepository;
import com.attsw.bookstore.repository.CategoryRepository;

import io.restassured.RestAssured;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class BookRestControllerIT {

	@Container
	static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:5.7")
			.withDatabaseName("test_bookstore")
			.withUsername("test")
			.withPassword("test");

	@DynamicPropertySource
	static void databaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
	}

	@LocalServerPort
	private int port;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@BeforeEach
	void setup() {
		RestAssured.port = port;
		bookRepository.deleteAll();
		categoryRepository.deleteAll();
	}

	@Test
	void testGetAllBooks_whenEmpty() {
		given()
			.accept(MediaType.APPLICATION_JSON_VALUE)
		.when()
			.get("/api/books")
		.then()
			.statusCode(200)
			.body("$", hasSize(0));
	}

	@Test
	void testGetAllBooks_withBooks() {
	    Category category = new Category();
	    category.setName("Fiction");
	    category = categoryRepository.save(category);

	    Book book1 = new Book();
	    book1.setTitle("Book1");
	    book1.setAuthor("Author1");
	    book1.setIsbn("1111111111");
	    book1.setPublishedDate(LocalDate.of(2024, 1, 15));
	    book1.setAvailable(true);
	    book1.setCategory(category);
	    bookRepository.save(book1);

	    Book book2 = new Book();
	    book2.setTitle("Book2");
	    book2.setAuthor("Author2");
	    book2.setIsbn("2222222222");
	    book2.setPublishedDate(LocalDate.of(2024, 2, 20));
	    book2.setAvailable(false);
	    book2.setCategory(category);
	    bookRepository.save(book2);

	    given()
	        .accept(MediaType.APPLICATION_JSON_VALUE)
	    .when()
	        .get("/api/books")
	    .then()
	        .statusCode(200)
	        .body("$", hasSize(2))
	        .body("title", hasItems("Book1", "Book2"));
	}

	@Test
	void testGetBookById_found() {
	    Category category = new Category();
	    category.setName("Fiction");
	    category = categoryRepository.save(category);

	    Book book = new Book();
	    book.setTitle("Test Book");
	    book.setAuthor("Test Author");
	    book.setIsbn("1234567890");
	    book.setPublishedDate(LocalDate.of(2024, 1, 1));
	    book.setAvailable(true);
	    book.setCategory(category);
	    Book saved = bookRepository.save(book);

	    given()
	        .accept(MediaType.APPLICATION_JSON_VALUE)
	    .when()
	        .get("/api/books/" + saved.getId())
	    .then()
	        .statusCode(200)
	        .body("id", equalTo(saved.getId().intValue()))
	        .body("title", equalTo("Test Book"))
	        .body("author", equalTo("Test Author"))
	        .body("isbn", equalTo("1234567890"))
	        .body("available", equalTo(true))
	        .body("category.name", equalTo("Fiction"));
	}

	@Test
	void testGetBookById_notFound() {
		given()
			.accept(MediaType.APPLICATION_JSON_VALUE)
		.when()
			.get("/api/books/999")
		.then()
			.statusCode(404);
	}

	@Test
	void testCreateBook() {
	    Category category = new Category();
	    category.setName("Fiction");
	    category = categoryRepository.save(category);

	    String newBookJson = String.format(
	        "{\"title\":\"New Book\"," +
	        "\"author\":\"New Author\"," +
	        "\"isbn\":\"1111111111\"," +
	        "\"publishedDate\":\"2024-01-15\"," +
	        "\"available\":true," +
	        "\"category\":{\"id\":%d}}",
	        category.getId()
	    );

	    Integer bookId = given()
	        .contentType(MediaType.APPLICATION_JSON_VALUE)
	        .body(newBookJson)
	    .when()
	        .post("/api/books")
	    .then()
	        .statusCode(201)
	        .body("title", equalTo("New Book"))
	        .body("author", equalTo("New Author"))
	        .body("isbn", equalTo("1111111111"))
	        .body("available", equalTo(true))
	        .body("category.id", equalTo(category.getId().intValue()))
	        .extract().path("id");

	    Book dbBook = bookRepository.findById(bookId.longValue()).orElse(null);
	    assertThat(dbBook).isNotNull();
	    assertThat(dbBook.getTitle()).isEqualTo("New Book");
	    assertThat(dbBook.getAuthor()).isEqualTo("New Author");
	    assertThat(dbBook.getIsbn()).isEqualTo("1111111111");
	    assertThat(dbBook.getPublishedDate()).isEqualTo(LocalDate.of(2024, 1, 15));
	    assertThat(dbBook.isAvailable()).isTrue();
	    assertThat(dbBook.getCategory().getId()).isEqualTo(category.getId());
	}

	@Test
	void testUpdateBook() {
	    Category oldCategory = new Category();
	    oldCategory.setName("Fiction");
	    oldCategory = categoryRepository.save(oldCategory);

	    Category newCategory = new Category();
	    newCategory.setName("Science");
	    newCategory = categoryRepository.save(newCategory);

	    Book book = new Book();
	    book.setTitle("Old Title");
	    book.setAuthor("Old Author");
	    book.setIsbn("0000000000");
	    book.setPublishedDate(LocalDate.of(2023, 1, 1));
	    book.setAvailable(false);
	    book.setCategory(oldCategory);
	    book = bookRepository.save(book);

	    String updateJson = String.format(
	        "{\"title\":\"Updated Title\"," +
	        "\"author\":\"Updated Author\"," +
	        "\"isbn\":\"9999999999\"," +
	        "\"publishedDate\":\"2024-12-31\"," +
	        "\"available\":true," +
	        "\"category\":{\"id\":%d}}",
	        newCategory.getId()
	    );

	    given()
	        .contentType(MediaType.APPLICATION_JSON_VALUE)
	        .body(updateJson)
	    .when()
	        .put("/api/books/" + book.getId())
	    .then()
	        .statusCode(200)
	        .body("title", equalTo("Updated Title"))
	        .body("author", equalTo("Updated Author"))
	        .body("isbn", equalTo("9999999999"))
	        .body("available", equalTo(true))
	        .body("category.id", equalTo(newCategory.getId().intValue()));

	    Book dbBook = bookRepository.findById(book.getId()).orElse(null);
	    assertThat(dbBook).isNotNull();
	    assertThat(dbBook.getTitle()).isEqualTo("Updated Title");
	    assertThat(dbBook.getAuthor()).isEqualTo("Updated Author");
	    assertThat(dbBook.getIsbn()).isEqualTo("9999999999");
	    assertThat(dbBook.getPublishedDate()).isEqualTo(LocalDate.of(2024, 12, 31));
	    assertThat(dbBook.isAvailable()).isTrue();
	    assertThat(dbBook.getCategory().getId()).isEqualTo(newCategory.getId());
	}

	@Test
	void testDeleteBook() {
	    Category category = new Category();
	    category.setName("Fiction");
	    category = categoryRepository.save(category);

	    Book book = new Book();
	    book.setTitle("To Delete");
	    book.setAuthor("Delete Author");
	    book.setIsbn("0000000000");
	    book.setPublishedDate(LocalDate.of(2024, 1, 1));
	    book.setAvailable(true);
	    book.setCategory(category);
	    book = bookRepository.save(book);
	    Long bookId = book.getId();

	    given()
	    .when()
	        .delete("/api/books/" + bookId)
	    .then()
	        .statusCode(204);

	    assertThat(bookRepository.findById(bookId)).isEmpty();
	}
}
