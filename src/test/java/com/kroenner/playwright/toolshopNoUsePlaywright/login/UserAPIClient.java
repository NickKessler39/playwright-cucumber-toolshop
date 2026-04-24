package com.kroenner.playwright.toolshopNoUsePlaywright.login;

import com.kroenner.playwright.toolshopNoUsePlaywright.domain.User;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.RequestOptions;
import io.qameta.allure.Step;

public class UserAPIClient {
    private final Page page;

    private static final String REGISTER_USER = "https://api.practicesoftwaretesting.com/users/register";

    public UserAPIClient(Page page) {
        this.page = page;
    }

    @Step("Register a new user through API")
    public void registerUser(User user) {
        var response = page.request().post(REGISTER_USER, RequestOptions.create()
                .setData(user)
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                );
        if (response.status() != 201) {
            throw new IllegalArgumentException("Could not create user: " + response.text());
        }
    }
}
