package com.kroenner.playwright.toolshopNoUsePlaywright.catalog;

import com.kroenner.playwright.toolshopNoUsePlaywright.catalog.pageobjects.ProductList;
import com.kroenner.playwright.toolshopNoUsePlaywright.catalog.pageobjects.SearchComponent;
import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.PlaywrightAbstractTestCase;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

//@Execution(ExecutionMode.SAME_THREAD) //Здесь мы делаем override наших properties, даже если там будет стоять
//junit.jupiter.execution.parallel.mode.default=concurrent, то в этом тестовом классе тесты будут бежать друг за другом - SAME_THREAD
@DisplayName("Search Tests")
@Feature("Search")
public class SearchForProductsTest extends PlaywrightAbstractTestCase {
    SearchComponent searchComponent;
    ProductList productList;

    @BeforeEach
    void openTheCataloguePage() {
        openPage();
        setUp();
    }
    void openPage() {
        page.navigate("https://practicesoftwaretesting.com/");
    }

    void setUp() {
        searchComponent = new SearchComponent(page);
        productList = new ProductList(page);
    }

    @DisplayName("Search a product by keyword")
    @Story("Basic search function")
    @Test
    void whenSearchingByKeyword() {
        searchComponent.searchBy("tape");
        var matchingProducts = productList.getProductNames();
        Assertions.assertThat(matchingProducts).contains("Tape Measure 7.5m", "Measuring Tape", "Tape Measure 5m");
    }

    @DisplayName("Negative test with unknown keyword")
    @Story("Basic search function")
    @Test
    void whenThereIsNoMatchingProduct() {
        searchComponent.searchBy("unknown");
        var matchingProducts = productList.getProductNames();
        Assertions.assertThat(matchingProducts).isEmpty();
        Assertions.assertThat(productList.getSearchCompletedMessage()).contains("There are no products found");
    }

    @DisplayName("Clearing the search results")
    @Story("Basic search function")
    @Test
    void clearingTheSearchResults() {
        searchComponent.searchBy("saw");
        searchComponent.clearSearch();
        var matchingProducts = productList.getProductNames();
        Assertions.assertThat(matchingProducts).hasSize(9);
    }

}
