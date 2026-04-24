package com.kroenner.playwright.toolshopNoUsePlaywright.login.pages;

import com.kroenner.playwright.toolshopNoUsePlaywright.domain.User;
import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.ScreenshotManager;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

public class LoginPage {
    private Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    @Step("Open login page")
    public void open() {
        page.navigate("https://practicesoftwaretesting.com/auth/login");
        ScreenshotManager.takeScreenshot(page, "Login page opened");
    }

    @Step("Login as {user.email}")
    public void loginAs(User user) {
        Allure.step("Login as " + user.email(), () -> {
            page.getByPlaceholder("Your email").fill(user.email());
            page.getByPlaceholder("Your password").fill(user.password());
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();

            ScreenshotManager.takeScreenshot(page, "User " + user.email() + " has logged in");
        });
    }

    public String title() {
        return page.getByTestId("page-title").textContent();
    }

    public String loginErrorMessage() {
        return page.getByTestId("login-error").textContent();
    }
}
