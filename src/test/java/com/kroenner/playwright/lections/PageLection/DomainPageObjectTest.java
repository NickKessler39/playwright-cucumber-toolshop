package com.kroenner.playwright.lections.PageLection;

import com.kroenner.playwright.lections.PageLection.LectionDomain.CartLineItem;
import com.kroenner.playwright.lections.PageLection.LectionPages.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DomainPageObjectTest {
    protected static Playwright playwright;
    protected static Browser browser;
    protected static BrowserContext browserContext;
    Page page;


    @BeforeAll
    static void setUpBrowser() {
        playwright = Playwright.create();
        playwright.selectors().setTestIdAttribute("data-test");
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).
                        setArgs(Arrays.asList("--no-sandbox", "--disable-extensions", "--disable-notifications"))
        );
    }

    @BeforeEach
    void setUpBrowserContext() {
        browserContext = browser.newContext();
        page = browserContext.newPage();
        page.navigate("https://practicesoftwaretesting.com/");
    }

    @AfterEach
    void closeContext() {
        browserContext.close();
    }

    @AfterAll
    static void tearDown() {
        browser.close();
        playwright.close();
    }

    @Nested
    class WhenAddingItemsToTheCart {

        @DisplayName("Without Page Objects")
        @Test
        void withoutPageObjects() {
            page.waitForResponse(("**/products/search?q=pliers"), () -> {
                page.getByPlaceholder("Search").fill("pliers");
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
            });
            page.locator(".card").getByText("Combination Pliers").click();
            page.getByTestId("increase-quantity").click();
            page.getByTestId("increase-quantity").click();
            page.getByText("Add to cart").click();
            page.waitForCondition(() -> page.getByTestId("cart-quantity").textContent().equals("3"));
            page.getByTestId("nav-cart").click();

            assertThat(page.locator(".product-title").getByText(("Combination Pliers")).isVisible());
            assertThat(page.getByTestId("cart-quantity").getByText(("3")).isVisible());
        }

        @DisplayName("With Page Objects")
        @Test
        void withPageObjects() {
            SearchComponent searchComponent = new SearchComponent(page);
            ProductList productList = new ProductList(page);
            ProductDetails productDetails = new ProductDetails(page);
            NavBar navBar = new NavBar(page);
            CheckoutCart checkoutCart = new CheckoutCart(page);

            searchComponent.searchBy("pliers");
            productList.viewProductDetails("Combination Pliers");
            productDetails.increaseQuantityTo("Combination Pliers", 6);
            productDetails.addToCart();

            navBar.openCart();
            List<CartLineItem> lineItems = checkoutCart.getLineItems();

            assertThat(lineItems)
                    .hasSize(1)
                    .first()
                    .satisfies(item -> {
                                assertThat(item.title()).contains("Combination Pliers");
                                assertThat(item.quantity()).isEqualTo(6);
                                assertThat(item.total()).isEqualTo(item.quantity() * item.price());
                            });
        }
    }
}
