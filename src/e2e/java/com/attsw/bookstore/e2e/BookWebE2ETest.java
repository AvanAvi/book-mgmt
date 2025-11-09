package com.attsw.bookstore.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
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
 * End-to-End tests for Book Web UI using Selenium WebDriver.
 * Requires the Spring Boot application to be running.
 */
class BookWebE2ETest {

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

    /**
     * Tests that the home page loads with correct title.
     */
    @Test
    void testHomePageTitle() {
        driver.get(baseUrl);
        assertThat(driver.getTitle()).isEqualTo("Book Management");
    }

    /**
     * Tests navigation to the new book creation page.
     */
    @Test
    void testNavigateToNewBookPage() {
        driver.get(baseUrl);
        driver.findElement(By.linkText("Books")).click();
        driver.findElement(By.linkText("+ New Book")).click();
        assertThat(driver.getCurrentUrl()).contains("/books/new");
    }

    /**
     * Tests creating a new book through the web form.
     */
    @Test
    void testCreateNewBook() {
        driver.get(baseUrl + "/books/new");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));

        // Fill form fields
        driver.findElement(By.id("title")).sendKeys("E2E Test Book");
        driver.findElement(By.id("author")).sendKeys("E2E Author");
        driver.findElement(By.id("isbn")).sendKeys("1234567890");
        
        // Set date using JavaScript
        WebElement createDateInput = driver.findElement(By.id("publishedDate"));
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].value = '2024-11-04';", createDateInput);
        
        driver.findElement(By.id("available")).click();
        driver.findElement(By.id("submitButton")).click();

        // Wait for redirect
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/books"),
                ExpectedConditions.urlToBe(baseUrl + "/")
        ));

        // Verify book was created
        driver.get(baseUrl + "/books");
        assertThat(driver.getPageSource()).contains("E2E Test Book");
    }
    
    /**
     * Tests viewing the books list page.
     */
    @Test
    void testViewAllBooks() {
        driver.get(baseUrl + "/books");
        assertThat(driver.findElements(By.id("booksTable"))).isNotEmpty();
    }

    /**
     * Tests editing an existing book through the web form.
     */
    @Test
    void testEditBook() {
        driver.get(baseUrl + "/books");

        // Find first edit button if exists
        var editButtons = driver.findElements(By.cssSelector("a[href*='/books/'][href*='/edit']"));

        if (!editButtons.isEmpty()) {
            editButtons.get(0).click();

            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("title")));

            // Verify on edit page
            assertThat(driver.getCurrentUrl()).contains("/books/");
            assertThat(driver.getCurrentUrl()).contains("/edit");

            // Update form fields
            var titleInput = driver.findElement(By.id("title"));
            titleInput.clear();
            titleInput.sendKeys("Updated Title");

            var authorInput = driver.findElement(By.id("author"));
            authorInput.clear();
            authorInput.sendKeys("Updated Author");

            var isbnInput = driver.findElement(By.id("isbn"));
            isbnInput.clear();
            isbnInput.sendKeys("9999999999");

            WebElement editDateInput = driver.findElement(By.id("publishedDate"));
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = '2024-12-31';", editDateInput);

            // Ensure available is checked
            var availableCheckbox = driver.findElement(By.id("available"));
            if (!availableCheckbox.isSelected()) {
                availableCheckbox.click();
            }

            driver.findElement(By.id("submitButton")).click();

            // Wait for redirect
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/books"),
                    ExpectedConditions.urlToBe(baseUrl + "/")
            ));

            // Verify update
            driver.get(baseUrl + "/books");
            assertThat(driver.getPageSource()).contains("Updated Title");
        }
    }

    /**
     * Tests deleting a book and verifying the count decreases.
     */
    @Test
    void testDeleteBook() {
        driver.get(baseUrl + "/books");

        // Count books before deletion
        var deleteButtonsBefore = driver.findElements(By.cssSelector("form[action*='/books/'][action*='/delete'] button"));
        int countBefore = deleteButtonsBefore.size();

        if (countBefore > 0) {
            deleteButtonsBefore.get(0).click();

            // Wait for page refresh
            wait.until(ExpectedConditions.stalenessOf(deleteButtonsBefore.get(0)));

            // Count books after deletion
            var deleteButtonsAfter = driver.findElements(By.cssSelector("form[action*='/books/'][action*='/delete'] button"));
            int countAfter = deleteButtonsAfter.size();

            // Verify deletion
            assertThat(countAfter).isLessThan(countBefore);
        }
    }
}