package com.attsw.bookstore.e2e;


import static org.assertj.core.api.Assertions.assertThat;  

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * End-to-End test for Book Web UI.
 * Assumes Spring Boot application is ALREADY RUNNING.
 * Uses Selenium WebDriver to test the web interface.
 */
class BookWebE2E { // NOSONAR

	private static int port = Integer.parseInt(System.getProperty("server.port", "9090"));
	private static String baseUrl = "http://localhost:" + port;

	private WebDriver driver;
	private WebDriverWait wait;

	@BeforeAll
	static void setupClass() {
		WebDriverManager.chromedriver().setup();
	}

	@BeforeEach
	void setup() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		driver = new ChromeDriver(options);
		wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	}

	@AfterEach
	void teardown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@Test
	void testHomePageTitle() {
		driver.get(baseUrl);
		assertThat(driver.getTitle()).isEqualTo("Book Management");
	}

	@Test
	void testNavigateToNewBookPage() {
		driver.get(baseUrl);
		driver.findElement(By.linkText("Books")).click();
		driver.findElement(By.linkText("+ New Book")).click();
		assertThat(driver.getCurrentUrl()).contains("/books/new");
	}

	@Test
	void testCreateNewBook() {
		// Navigate to new book page
		driver.get(baseUrl + "/books/new");

		// Wait for form to load
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));

		// Fill form
		driver.findElement(By.id("title")).sendKeys("E2E Test Book");

		// Select first category (skip "-- No Category --")
		driver.findElement(By.id("category")).click();
		driver.findElement(By.cssSelector("#category option:nth-child(2)")).click();

		// Submit form
		driver.findElement(By.id("submitButton")).click();

		// Wait for redirect to complete
		wait.until(ExpectedConditions.or(
			ExpectedConditions.urlContains("/books"),
			ExpectedConditions.urlToBe(baseUrl + "/")
		));

		// Navigate to books page to verify
		driver.get(baseUrl + "/books");

		// Verify book is listed
		assertThat(driver.getPageSource()).contains("E2E Test Book");
	}

	@Test
	void testViewAllBooks() {
		driver.get(baseUrl + "/books");

		// Verify books table exists
		assertThat(driver.findElements(By.id("booksTable"))).isNotEmpty();
	}

	@Test
	void testEditBook() {
		driver.get(baseUrl + "/books");

		// Find first edit button (if exists)
		var editButtons = driver.findElements(By.cssSelector("a[href*='/books/'][href*='/edit']"));

		if (!editButtons.isEmpty()) {
			editButtons.get(0).click();

			// Wait for edit page to load
			wait.until(ExpectedConditions.presenceOfElementLocated(By.name("title")));

			// Verify we're on edit page
			assertThat(driver.getCurrentUrl()).contains("/books/");
			assertThat(driver.getCurrentUrl()).contains("/edit");

			// Change title
			var titleInput = driver.findElement(By.id("title"));
			titleInput.clear();
			titleInput.sendKeys("Updated E2E Book");

			// Submit
			driver.findElement(By.id("submitButton")).click();

			// Wait for redirect
			wait.until(ExpectedConditions.or(
				ExpectedConditions.urlContains("/books"),
				ExpectedConditions.urlToBe(baseUrl + "/")
			));

			// Navigate to books page to verify
			driver.get(baseUrl + "/books");

			// Verify update
			assertThat(driver.getPageSource()).contains("Updated E2E Book");
		}
	}

	@Test
	void testDeleteBook() {
		driver.get(baseUrl + "/books");

		// Count books before deletion
		var deleteButtonsBefore = driver.findElements(By.cssSelector("form[action*='/books/'][action*='/delete'] button"));
		int countBefore = deleteButtonsBefore.size();

		if (countBefore > 0) {
			// Click first delete button
			deleteButtonsBefore.get(0).click();

			// Wait for page refresh
			wait.until(ExpectedConditions.stalenessOf(deleteButtonsBefore.get(0)));

			// Count books after deletion
			var deleteButtonsAfter = driver.findElements(By.cssSelector("form[action*='/books/'][action*='/delete'] button"));
			int countAfter = deleteButtonsAfter.size();

			// Verify one less book
			assertThat(countAfter).isLessThan(countBefore);
		}
	}
}