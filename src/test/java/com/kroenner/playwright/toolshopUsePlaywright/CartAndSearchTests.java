package com.kroenner.playwright.toolshopUsePlaywright;

import com.kroenner.playwright.toolshopUsePlaywright.pageobjects.*;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@UsePlaywright(HeadlessChromeOptions.class)
public class CartAndSearchTests implements TakesFinalScreenshot {

    SearchComponent searchComponent;
    ProductList productList;
    ProductDetails productDetails;
    NavBar navBar;
    CheckoutCart checkoutCart;

    @BeforeEach
    void openTheCataloguePage(Page page) {
        openPage(page);
        setUp(page);
    }
    void openPage(Page page) {
        page.navigate("https://practicesoftwaretesting.com/");
    }
    void setUp(Page page) {
        searchComponent = new SearchComponent(page);
        productList = new ProductList(page);
        productDetails = new ProductDetails(page);
        navBar = new NavBar(page);
        checkoutCart = new CheckoutCart(page);
    }

    @DisplayName("Cart test with Page Objects")
    @Test
    void cartTestWithPageObjects() {
        searchComponent.searchBy("pliers");
        productList.viewProductDetails("Combination Pliers");
        productDetails.increaseQuantityTo(6);
        productDetails.addToCart();

        navBar.openCart();
        List<CartLineItem> lineItems = checkoutCart.getLineItems();

        Assertions.assertThat(lineItems)
                .hasSize(1)
                .first()
                .satisfies(item -> {
                    Assertions.assertThat(item.title()).contains("Combination Pliers");
                    Assertions.assertThat(item.quantity()).isEqualTo(6);
                    Assertions.assertThat(item.total()).isEqualTo(item.quantity() * item.price());
                });
    }

    @DisplayName("Search a product by keyword")
    @Test
    void whenSearchingByKeyword() {
        searchComponent.searchBy("tape");
        var matchingProducts = productList.getProductNames();
        Assertions.assertThat(matchingProducts).contains("Tape Measure 7.5m", "Measuring Tape", "Tape Measure 5m");
    }

    @DisplayName("Negative test with unknown keyword")
    @Test
    void whenThereIsNoMatchingProduct() {
        searchComponent.searchBy("unknown");
        var matchingProducts = productList.getProductNames();
        Assertions.assertThat(matchingProducts).isEmpty();
        Assertions.assertThat(productList.getSearchCompletedMessage()).contains("There are no products found");
    }

    @Test
    void clearingTheSearchResults() {
        searchComponent.searchBy("saw");
        searchComponent.clearSearch();
        var matchingProducts = productList.getProductNames();
        Assertions.assertThat(matchingProducts).hasSize(9);
    }
}
