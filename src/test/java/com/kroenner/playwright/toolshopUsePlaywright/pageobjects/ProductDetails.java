package com.kroenner.playwright.toolshopUsePlaywright.pageobjects;

import com.microsoft.playwright.Page;

public class ProductDetails {
    private final Page page;
    private int currentAmount = 1; //Состояние по умолчанию

    public ProductDetails(Page page) {
        this.page = page;
    }

    public void increaseQuantityTo(int targetAmount) {
        if (targetAmount <= 1) return; // Если увеличивать не надо, просто выходим из метода
        for (int i = 1;  i <= targetAmount - 1; i++) {
            page.getByTestId("increase-quantity").click();
        }
        this.currentAmount = targetAmount; //запоминаем amonut
    }
    public void addToCart() {
        // 1. Ждем ответа от сервера (метод принимает два аргумента: условие и действие)
        page.waitForResponse(
                response -> response.url().contains("/carts") && response.request().method().equals("POST"), // Условие (лямбда)
        () -> page.getByText("Add to cart").click() // Действие, которое вызывает этот ответ
    );
        // 2. Ждем, пока счетчик в корзине обновится
        page.waitForCondition(() ->
                page.getByTestId("cart-quantity").textContent().equals(String.valueOf(currentAmount))); //Теперь метод берет число из памяти самого класса!
    }

}
