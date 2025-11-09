package com.attsw.bookstore.web;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.attsw.bookstore.model.Category;
import com.attsw.bookstore.service.BookService;
import com.attsw.bookstore.service.CategoryService;

/**
 * HTML-level tests for category web flows using HtmlUnit.
 */
@WebMvcTest(controllers = CategoryWebController.class)
class CategoryWebControllerHtmlUnitTest {

    @Autowired
    private WebClient webClient;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private BookService bookService;

    private String removeWindowsCR(String s) {
        return s.replace("\r", "");
    }

    @Test
    void testCategoriesListPageTitle() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(Collections.emptyList());

        HtmlPage page = webClient.getPage("/categories");
        assertThat(page.getTitleText()).isEqualTo("Categories");
    }

    @Test
    void testCategoriesListPageWithNoCategories() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(Collections.emptyList());

        HtmlPage page = webClient.getPage("/categories");

        HtmlElement categoriesList = page.getHtmlElementById("categoriesTable");
        assertThat(categoriesList).isNotNull();
        assertThat(categoriesList.getElementsByTagName("li")).isEmpty();
    }

    @Test
    void testCategoriesListPageWithCategories_ShouldShowThemInList() throws Exception {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Fiction");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Science");

        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(category1, category2));

        HtmlPage page = webClient.getPage("/categories");

        HtmlElement categoriesList = page.getHtmlElementById("categoriesTable");
        assertThat(categoriesList).isNotNull();

        String pageText = removeWindowsCR(page.asNormalizedText());
        assertThat(pageText).contains("Fiction", "Science");

        HtmlAnchor editLink1 = page.getAnchorByHref("/categories/1/edit");
        assertThat(editLink1).isNotNull();
        
        HtmlAnchor editLink2 = page.getAnchorByHref("/categories/2/edit");
        assertThat(editLink2).isNotNull();
    }

    @Test
    void testEditExistingCategory() throws Exception {
        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Original Name");

        when(categoryService.getCategoryById(1L)).thenReturn(existingCategory);

        HtmlPage page = webClient.getPage("/categories/1/edit");

        assertThat(page.getTitleText()).isEqualTo("Edit Category");

        HtmlForm form = page.getForms().get(0);
        assertThat(form).isNotNull();

        String pageText = removeWindowsCR(page.asNormalizedText());
        assertThat(pageText).contains("Original Name");

        HtmlInput nameInput = form.getInputByName("name");
        nameInput.setValue("Modified Name");
        
        assertThat(form.getButtonByName("btn_submit")).isNotNull();
    }

    @Test
    void testCreateNewCategory() throws Exception {
        HtmlPage page = webClient.getPage("/categories/new");

        assertThat(page.getTitleText()).isEqualTo("New Category");

        HtmlForm form = page.getForms().get(0);
        assertThat(form).isNotNull();

        HtmlInput nameInput = form.getInputByName("name");
        nameInput.setValue("New Category Name");
        
        assertThat(form.getButtonByName("btn_submit")).isNotNull();
    }

    @Test
    void testCategoriesListPage_ShouldProvideALinkForCreatingNewCategory() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(Collections.emptyList());

        HtmlPage page = webClient.getPage("/categories");

        HtmlAnchor newCategoryLink = page.getAnchorByText("+ New Category");
        assertThat(newCategoryLink.getHrefAttribute()).isEqualTo("/categories/new");
    }

    @Test
    void testCategoriesListPageWithCategories_ShouldShowEditLinks() throws Exception {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Category 1");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Category 2");

        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(category1, category2));

        HtmlPage page = webClient.getPage("/categories");

        HtmlElement categoriesList = page.getHtmlElementById("categoriesTable");
        assertThat(categoriesList).isNotNull();

        String listText = removeWindowsCR(categoriesList.asNormalizedText());
        assertThat(listText).contains("Category 1", "Category 2", "Edit");

        HtmlAnchor editLink1 = page.getAnchorByHref("/categories/1/edit");
        assertThat(editLink1).isNotNull();
        
        HtmlAnchor editLink2 = page.getAnchorByHref("/categories/2/edit");
        assertThat(editLink2).isNotNull();
    }

    @Test
    void testNewCategoryFormHasAllRequiredFields() throws Exception {
        HtmlPage page = webClient.getPage("/categories/new");

        HtmlForm form = page.getForms().get(0);

        HtmlInput nameInput = form.getInputByName("name");
        assertThat(nameInput).isNotNull();
        
        assertThat(form.getButtonByName("btn_submit")).isNotNull();
    }

    @Test
    void testEditCategoryFormHasAllRequiredFields() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        when(categoryService.getCategoryById(1L)).thenReturn(category);

        HtmlPage page = webClient.getPage("/categories/1/edit");

        HtmlForm form = page.getForms().get(0);
        
        HtmlInput nameInput = form.getInputByName("name");
        assertThat(nameInput).isNotNull();
        
        assertThat(form.getButtonByName("btn_submit")).isNotNull();
    }
}