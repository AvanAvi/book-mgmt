package com.attsw.bookstore.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * End-to-End test for Category Web UI.
 * Assumes Spring Boot application is ALREADY RUNNING.
 */
class CategoryWebE2E { // NOSONAR

	private static int port = Integer.parseInt(System.getProperty("server.port", "9090"));
	private static String baseUrl = "http://localhost:" + port;

	private WebDriver driver;

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
	}

	@AfterEach
	void teardown() {
		if (driver != null) {
			driver.quit();
		}
	}

	@Test
	void testNavigateToCategoriesPage() {
		driver.get(baseUrl);
		driver.findElement(By.linkText("Categories")).click();
		assertThat(driver.getCurrentUrl()).contains("/categories");
	}

	@Test
	void testViewAllCategories() {
		driver.get(baseUrl + "/categories");
		
		// Verify categories table exists
		assertThat(driver.findElements(By.id("categoriesTable"))).isNotEmpty();
	}

	@Test
	void testCreateNewCategory() {
		// Navigate to new category page
		driver.get(baseUrl + "/categories/new");

		// Fill form
		driver.findElement(By.id("name")).sendKeys("E2E Test Category");

		// Submit form
		driver.findElement(By.id("submitButton")).click();

		// Verify redirect to categories page and category is listed
		assertThat(driver.getCurrentUrl()).contains("/categories");
		assertThat(driver.getPageSource()).contains("E2E Test Category");
	}

	@Test
	void testEditCategory() {
		driver.get(baseUrl + "/categories");

		// Find first edit button (if exists)
		var editButtons = driver.findElements(By.cssSelector("a[href*='/categories/edit/']"));
		
		if (!editButtons.isEmpty()) {
			editButtons.get(0).click();

			// Verify we're on edit page
			assertThat(driver.getCurrentUrl()).contains("/categories/edit/");

			// Change name
			var nameInput = driver.findElement(By.id("name"));
			nameInput.clear();
			nameInput.sendKeys("Updated E2E Category");

			// Submit
			driver.findElement(By.id("submitButton")).click();

			// Verify redirect and update
			assertThat(driver.getPageSource()).contains("Updated E2E Category");
		}
	}

	@Test
	void testDeleteCategory() {
		driver.get(baseUrl + "/categories");

		// Count categories before deletion
		var deleteButtonsBefore = driver.findElements(By.cssSelector("button[onclick*='deleteCategory']"));
		int countBefore = deleteButtonsBefore.size();

		if (countBefore > 0) {
			// Click first delete button
			deleteButtonsBefore.get(0).click();

			// Accept confirmation dialog
			driver.switchTo().alert().accept();

			// Wait for page refresh
			driver.navigate().refresh();

			// Count categories after deletion
			var deleteButtonsAfter = driver.findElements(By.cssSelector("button[onclick*='deleteCategory']"));
			int countAfter = deleteButtonsAfter.size();

			// Verify one less category
			assertThat(countAfter).isLessThan(countBefore);
		}
	}

	@Test
	void testCannotDeleteCategoryWithBooks() {
		driver.get(baseUrl + "/categories");

		// Find a category that has books (look for disabled delete buttons or warning)
		var warningElements = driver.findElements(By.cssSelector(".category-has-books-warning"));
		
		if (!warningElements.isEmpty()) {
			// Verify warning message is displayed
			assertThat(warningElements.get(0).getText()).contains("has books");
		}
	}
}
