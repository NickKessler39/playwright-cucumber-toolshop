package com.kroenner.playwright.toolshopUsePlaywright.pageobjects;

import com.microsoft.playwright.Page;

public class NavBar {
    private final Page page;
    public NavBar(Page page) {
        this.page = page;
    }
    public void openCart() {
        page.getByTestId("nav-cart").click();
    }
}
