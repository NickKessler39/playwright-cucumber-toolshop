package com.kroenner.playwright.lections;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@UsePlaywright(WaitingTests.MyOptions.class)

public class WaitingTests {
    public static class MyOptions implements OptionsFactory {
        @Override
        public Options getOptions() {
            return new Options()
                    .setHeadless(Boolean.parseBoolean(System.getProperty("headless", "true")))
                    .setTestIdAttribute("data-test")
                    .setLaunchOptions(new BrowserType.LaunchOptions()
                            .setArgs(Arrays.asList("--disable extensions", "--disable-notifications"))
                            .setSlowMo(100)
                    );

        }
    }

    @Nested //Делаем тестсьют, со своими BeforeEach
    class WaitingForState {
        String mainPage = "https://practicesoftwaretesting.com/"; //может использоваться всеми тестами из Nested набора

        @BeforeEach
        void openTheCataloguePage(Page page) {
            openPage(page);
            page.waitForSelector(".card-img-top"); //есть ли элементы с классом .card-img-top?
        }
        void openPage(Page page) {
            page.navigate(mainPage);
        }

        @Test
        void shouldShowAllProductNames(Page page) {
            page.waitForCondition(() -> page.getByTestId("product-name").count() > 0); //ждем, что есть хоть 1 элемент с id, "() ->" это лямбда, означает "Ходи и проверяй, пока не увидишь что-то"
            page.waitForSelector("[data-test^='product-']"); //ждем, пока будет виден продукт с аттрибутом data-test, который начинается с "product-"
            page.waitForSelector("//* [starts-with(@data-test, 'product-')]"); //то же самое, но xpath
            List<String> productNames = page.getByTestId("product-name").allInnerTexts();
            Assertions.assertThat(productNames).contains("Pliers", "Bolt Cutters", "Hammer");
        }

        @Test
        void shouldShowAllProductImages(Page page) {
            page.waitForCondition(() -> page.locator(".card .card-img-top").first().isVisible());
            page.waitForSelector(".card-img-top");
            assertThat(page.locator(".card img").first()).isVisible();
            assertThat(page.getByAltText("Pliers", new Page.GetByAltTextOptions().setExact(true))).isVisible();
            assertThat(page.getByRole(AriaRole.IMG, new Page.GetByRoleOptions().setName("Pliers").setExact(true))).isVisible();

            List<String> productImageTitles = page.locator(".card-img-top").all()
                    .stream()
                    .map(img -> img.getAttribute("alt"))
                    .toList();

            Assertions.assertThat(productImageTitles).contains("Pliers", "Bolt Cutters", "Hammer");
        }
        @Test
        void shouldWaitforTheFilterCheckboxes(Page page) {
            var screwDriverFilter = page.getByLabel("Screwdriver");

            screwDriverFilter.click();
            assertThat(screwDriverFilter).isChecked();

            screwDriverFilter.uncheck();
            assertThat(screwDriverFilter).not().isChecked();
        }

        @Test
        void shouldFilterProductsByCategory(Page page) {
            page.getByRole(AriaRole.MENUBAR).getByText("Categories").click();
            page.getByRole(AriaRole.MENUBAR).getByText("Power Tools").click();
            page.locator(".card .card-img-top").first().waitFor();
            assertThat(page.locator(".card .card-img-top").first()).isVisible();
            page.waitForSelector(".card",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(2000));
            var filteredProducts = page.getByTestId("product-name").allInnerTexts();

            Assertions.assertThat(filteredProducts).contains("Sheet Sander", "Belt Sander", "Circular Saw");
        }

        @Test
        void shouldDisplayToasterMessage(Page page) {
            page.getByText("Bolt Cutters").click();
            page.getByText("Add to cart").click();

            // Wait for the toaster message to appear
            assertThat(page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setName("Product added to shopping cart"))).isVisible();
            assertThat(page.getByRole(AriaRole.ALERT)).isVisible();
            assertThat(page.getByRole(AriaRole.ALERT)).hasText(Pattern.compile("Product added to shopping cart"));
            assertThat(page.getByRole(AriaRole.ALERT)).containsText("Product added to shopping cart");

            page.waitForCondition( () -> page.getByRole(AriaRole.ALERT).isHidden()); //опять лямбда, требуем снова и снова проверять, пока элемент не пропадет
        }

        @Test
        void shouldUpdateCartItemCount(Page page) {
            page.getByText("Bolt Cutters").click();
            page.getByText("Add to cart").click();

            page.waitForCondition( () -> page.getByTestId("cart-quantity").textContent().equals("1"));
            page.waitForSelector("[data-test=cart-quantity]:has-text('1')");
        }
     }
}
