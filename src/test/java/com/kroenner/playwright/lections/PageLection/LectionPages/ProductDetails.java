package com.kroenner.playwright.lections.PageLection.LectionPages;

import com.microsoft.playwright.Page;

public class ProductDetails {
    private final Page page;
    private int currentAmount = 1; //Состояние по умолчанию

    public ProductDetails(Page page) {
        this.page = page;
    }

    public void increaseQuantityTo(String keyword, int targetAmount) {
        if (targetAmount <= 1) return; // Если увеличивать не надо, просто выходим из метода
        for (int i = 1;  i <= targetAmount - 1; i++) {
            page.getByTestId("increase-quantity").click();
        }
        this.currentAmount = targetAmount; //запоминаем amount
    }
    public void addToCart() {
        // 1. Ждем ответа от сервера (метод принимает два аргумента: условие и действие)
        page.waitForResponse(
                response -> response.url().contains("/carts") && response.request().method().equals("POST"), // Условие (лямбда)
                () -> page.getByText("Add to cart").click() // Действие, которое вызывает этот ответ
        );
        page.waitForCondition(() ->
                page.getByTestId("cart-quantity").textContent().equals(String.valueOf(currentAmount))); //Теперь метод берет число из памяти самого класса!

    }
    public void addToCartAndCheckTheAmount(int expectedAmount) {
        page.waitForResponse(
                response -> response.url().contains("/carts") && response.request().method().equals("POST"),
                () -> page.getByText("Add to cart").click()
        );
        page.waitForCondition(() ->
                page.getByTestId("cart-quantity").textContent().equals(String.valueOf(expectedAmount)));
    }
}
