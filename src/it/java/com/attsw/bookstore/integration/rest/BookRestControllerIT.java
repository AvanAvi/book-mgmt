package com.attsw.bookstore.integration.rest;

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

/**
 * TRUE Integration Test - Tests full stack with real MySQL 5.7 database.
 * NO mocks - all layers are real.
 */
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
		book1.setCategory(category);
		bookRepository.save(book1);

		Book book2 = new Book();
		book2.setTitle("Book2");
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
			"{\"title\":\"New Book\",\"category\":{\"id\":%d}}",
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
			.body("category.id", equalTo(category.getId().intValue()))
			.extract().path("id");

		Book dbBook = bookRepository.findById(bookId.longValue()).orElse(null);
		assertThat(dbBook).isNotNull();
		assertThat(dbBook.getTitle()).isEqualTo("New Book");
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
		book.setCategory(oldCategory);
		book = bookRepository.save(book);

		String updateJson = String.format(
			"{\"title\":\"Updated Title\",\"category\":{\"id\":%d}}",
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
			.body("category.id", equalTo(newCategory.getId().intValue()));

		Book dbBook = bookRepository.findById(book.getId()).orElse(null);
		assertThat(dbBook).isNotNull();
		assertThat(dbBook.getTitle()).isEqualTo("Updated Title");
		assertThat(dbBook.getCategory().getId()).isEqualTo(newCategory.getId());
	}

	@Test
	void testDeleteBook() {
		Category category = new Category();
		category.setName("Fiction");
		category = categoryRepository.save(category);

		Book book = new Book();
		book.setTitle("To Delete");
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
