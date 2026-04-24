package com.kroenner.playwright.lections.PageLection.LectionDomain;

import com.kroenner.playwright.lections.PageLection.LectionPages.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PlaywrightPageObjectTest {
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
        //Выносим объекты страниц из тестового метода в тестовый класс, убираем их инициализацию ("создание экземпляров" или же "инстанциация")
        SearchComponent searchComponent;
        ProductList productList;
        ProductDetails productDetails;
        NavBar navBar;
        CheckoutCart checkoutCart;

        @BeforeEach
        void setUp() {
            //Теперь будем создавать объекты страниц тут, мы их "инстанцируем"
            searchComponent = new SearchComponent(page);
            productList = new  ProductList(page);
            productDetails = new ProductDetails(page);
            navBar = new NavBar(page);
            checkoutCart = new CheckoutCart(page);
        }

        @DisplayName("With Page Objects")
        @Test
        void withPageObjects() {
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

        @Test
        void whenCheckingOutMultipleItems() {
            navBar.openHomePage();
            productList.viewProductDetails("Bolt Cutters");
            productDetails.increaseQuantityTo("Bolt Cutters", 2);
            productDetails.addToCartAndCheckTheAmount(2);

            navBar.openHomePage();
            productList.viewProductDetails("Claw Hammer");
            productDetails.increaseQuantityTo("Claw Hammer", 2);
            productDetails.addToCartAndCheckTheAmount(4);

            navBar.openCart();
            List<CartLineItem> lineItems = checkoutCart.getLineItems();
            /*
            Представь, что тебе нужно объяснить Java: «Возьми каждый объект из списка и вызови у него метод title()».
            Через лямбду это выглядело бы так: item -> item.title()
            Через ссылку на метод это выглядит так: CartLineItem::title
            Это буквально означает: «Используй метод title класса CartLineItem». Это просто более короткая и «чистая» запись той же самой лямбды.
            */
            Assertions.assertThat(lineItems).hasSize(2);
            Assertions.assertThat(lineItems).extracting(CartLineItem::title).containsExactlyInAnyOrder("Bolt Cutters", "Claw Hammer");
            /* Тоже самое
            List<String> productNames = lineItems.stream().map(CartLineItem::title).toList();
            Assertions.assertThat(productNames).contains("Bolt Cutters", "Claw Hammer");
            */

            Assertions.assertThat(lineItems)
                    .allSatisfy(item -> {
                        Assertions.assertThat(item.quantity()).isGreaterThanOrEqualTo(1);
                        Assertions.assertThat(item.price()).isGreaterThan(0.0);
                        Assertions.assertThat(item.total()).isGreaterThan(0.0);
                        Assertions.assertThat(item.total()).isEqualTo(item.quantity() * item.price());
                    });
        }
    }
}
