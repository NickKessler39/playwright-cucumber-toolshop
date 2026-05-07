package com.kroenner.playwright.lections;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import com.microsoft.playwright.options.AriaRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@UsePlaywright(AddingItemsToTheCartTest.MyOptions.class)
public class AddingItemsToTheCartTest {
    public static class MyOptions implements OptionsFactory {
        @Override
        public Options getOptions() {
            return new Options()
                    .setHeadless(Boolean.parseBoolean(System.getProperty("headless", "true")))
                    .setTestIdAttribute("data-test")
                    .setLaunchOptions(
                            new BrowserType.LaunchOptions()
                                    .setArgs(Arrays.asList("--disable-extensions", "--disable-notifications"))
                                    //.setSlowMo(500)
                    );
        }
    }

    @BeforeEach
    void openTheCataloguePage(Page page) {
        openPage(page);
    }

    void openPage(Page page) {
        page.navigate("https://practicesoftwaretesting.com/");
    }

    @DisplayName("Search for pliers")
    @Test
    void searchForPliers(Page page) {
        page.getByPlaceholder("Search").fill("Pliers");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
        assertThat(page.locator(".card")).hasCount(4);

        List<String> productNames = page.getByTestId("product-name").allTextContents();
        Assertions.assertThat(productNames).allMatch(name -> name.contains("Pliers"));

        Locator outOfStockItem = page.locator(".card")
                .filter(new Locator.FilterOptions().setHasText("Out of stock"))
                .getByTestId("product-name");

        assertThat(outOfStockItem).hasCount(1);
        assertThat(outOfStockItem).hasText("Long Nose Pliers");
    }
}
