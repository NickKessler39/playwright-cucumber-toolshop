package com.kroenner.playwright.toolshopNoUsePlaywright.catalog;

import com.kroenner.playwright.toolshopNoUsePlaywright.catalog.pageobjects.*;
import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.PlaywrightAbstractTestCase;
import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.WithTracing;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("Cart Tests")
@Feature("Shopping Cart")
public class CartTests extends PlaywrightAbstractTestCase implements WithTracing {

    SearchComponent searchComponent;
    ProductList productList;
    ProductDetails productDetails;
    NavBar navBar;
    CheckoutCart checkoutCart;

    @BeforeEach
    void openTheCataloguePage() {
       // setupTrace(); вывели tracing в отдельный fixture
        setUp(); //не забудь инициализировать метод, который отвечает за все page objects
        openPage();
    }
    void openPage() {
        navBar.openHomePage();
    }

    void setUp() {
        searchComponent = new SearchComponent(page);
        productList = new ProductList(page);
        productDetails = new ProductDetails(page);
        navBar = new NavBar(page);
        checkoutCart = new CheckoutCart(page);
        navBar = new NavBar(page);
    }
/* Вывели в отдельный fixture наш tracing
    void setupTrace() {
        browserContext.tracing().start(
                new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true)
        );
    }
*/

    /*
    @AfterEach

    void recordTrace(TestInfo testInfo) { //чтоб создавать trace.zip для каждого тестового метода отдельно
        browserContext.tracing().stop(
                new Tracing.StopOptions()
                        .setPath(Paths.get("target/tracer/trace-" + testInfo.getTestMethod() + ".zip")) //указываем путь
                );
    }
    */

    @DisplayName("Cart test with Page Objects")
    @Story("Smoke test for cart functionality")
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
}
