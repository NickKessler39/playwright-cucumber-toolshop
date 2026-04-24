package com.kroenner.playwright.lections.PageLection.LectionPages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;
import java.util.regex.Pattern;

public class ProductList {
    private final Page page;
    public ProductList(Page page) {
        this.page = page;
    }

    public List<String> getProductNames() {
        return page.getByTestId("product-name").allInnerTexts();
    }
    public void viewProductDetails(String productName) {
        page.locator(".card").getByText(productName, new Locator.GetByTextOptions().setExact(true)).click();
    }
}