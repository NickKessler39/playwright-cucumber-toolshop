package com.kroenner.playwright.toolshopNoUsePlaywright.login;

import com.kroenner.playwright.toolshopNoUsePlaywright.domain.Address;
import com.kroenner.playwright.toolshopNoUsePlaywright.domain.User;
import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.PlaywrightAbstractTestCase;
import com.kroenner.playwright.toolshopNoUsePlaywright.login.pages.LoginPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Login Tests")
@Feature("Login")
public class LoginWithRegisteredUserTest extends PlaywrightAbstractTestCase {
    @Test
    @DisplayName("Register new user and login")
    @Story("Login API tests")
    void should_login_with_registered_user() {
        //Register a user via the API
        Address manualAddress = new Address(  "Street 1",
                "City",
                "State",
                "Country",
                "1234AA");
        User manualUser = new User(
                "John",
                "Doe",
                manualAddress,
                "0987654321",
                "1970-01-01",
                "john@doe.example",
                "SuperSecure@123");
        //manualUser = в начале урока сначала использовали вручную заполненные данные, а также создавали объект типа Address, его также можно подставить в поля registerUser и loginAs

        User newUser = User.randomUser();
        UserAPIClient userAPIClient = new UserAPIClient(page);
        userAPIClient.registerUser(newUser);

        //Login via the login page
        LoginPage loginPage = new LoginPage(page);
        loginPage.open();
        loginPage.loginAs(newUser);
        //Check that we are on the right account page
        assertThat(loginPage.title()).contains("My account");
    }

    @Test
    @DisplayName("Register a new user and use wrong password")
    @Story("Login API tests")
    void should_reject_user_with_invalid_password() {
        User newUser = User.randomUser();
        UserAPIClient userAPIClient = new UserAPIClient(page);
        userAPIClient.registerUser(newUser);

        LoginPage loginPage = new LoginPage(page);
        loginPage.open();
        loginPage.loginAs(newUser.withPassword("wrong-password"));

        assertThat(loginPage.loginErrorMessage()).isEqualTo("Invalid email or password");
    }
}
