package com.kroenner.playwright.toolshopNoUsePlaywright.catalog.pageobjects;

import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.ProductSummary;
import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.ScreenshotManager;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import java.util.List;

public class ProductList {
    private final Page page;
    public ProductList(Page page) {
        this.page = page;
    }

    public List<String> getProductNames() {
        return page.getByTestId("product-name").allInnerTexts();
    }

    public List<ProductSummary> getProductSummaries() {
        return page.locator(".card").all() //Находит все карточки товаров на странице и превращает их в обычный список Java (List<Locator>).
                //Когда ты пишешь page.locator(".card").all(), Playwright находит на странице все элементы с классом .card (карточки товаров) и возвращает тебе List<Locator>.
                .stream() //Включает "конвейер". Теперь мы можем обрабатывать каждую карточку одну за другой.
                .map(productCard -> { //Это трансформатор. Он берет "сырой" локатор карточки (productCard) и превращает его в красивый объект ProductSummary.
                    String productName = productCard.getByTestId("product-name").textContent().strip(); //достаем имя и убираем пробелы по краям
                    String productPrice = productCard.getByTestId("product-price").textContent(); //достаем цену
                    return new ProductSummary(productName, productPrice); //создаем объект ProductSummary, в который кладем имя и цену
                }).toList(); //Собирает все созданные объекты обратно в список.
    }

    @Step("Open product details, product - {productName}")
    public void viewProductDetails(String productName) {
        page.locator(".card").getByText(productName).click();
        ScreenshotManager.takeScreenshot(page, "Product details, product - "+productName);
    }

    public String getSearchCompletedMessage() {
        return page.getByTestId("search_completed").textContent();
    }
}