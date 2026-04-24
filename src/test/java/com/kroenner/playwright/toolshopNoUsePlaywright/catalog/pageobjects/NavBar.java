package com.kroenner.playwright.toolshopNoUsePlaywright.catalog.pageobjects;

import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.ScreenshotManager;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class NavBar {
    private final Page page;
    public NavBar(Page page) {
        this.page = page;
    }

    @Step("Open cart")
    public void openCart() {
        page.getByTestId("nav-cart").click();
        ScreenshotManager.takeScreenshot(page, "Cart opened");
    }

    @Step("Open home page")
    public void openHomePage() {

        page.navigate("https://practicesoftwaretesting.com/");
        ScreenshotManager.takeScreenshot(page, "Home page opened");
    }

    @Step("Open contact page")
    public void toTheContactPage() {
        page.navigate("https://practicesoftwaretesting.com/contact");
        ScreenshotManager.takeScreenshot(page, "Contact page opened");
    }
}
