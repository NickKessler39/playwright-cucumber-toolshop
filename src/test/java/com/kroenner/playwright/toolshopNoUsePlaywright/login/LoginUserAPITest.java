package com.kroenner.playwright.toolshopNoUsePlaywright.login;

import com.kroenner.playwright.toolshopNoUsePlaywright.domain.User;
import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.PlaywrightAbstractTestCase;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.options.RequestOptions;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Login Tests")
@Feature("Login")
public class LoginUserAPITest extends PlaywrightAbstractTestCase {

    private APIRequestContext request;

    @BeforeEach
    void setup() {
        request = playwright.get().request().newContext(
                new APIRequest.NewContextOptions().setBaseURL("https://api.practicesoftwaretesting.com")
        );
    }

    @AfterEach
    void tearDown() {
        if (request != null) {
            request.dispose();
        }
    }

    @Test
    @DisplayName("Too many attempts to login with wrong credentials")
    @Story("Login API tests")
    void should_failToLoginUser() {
        //API ждет ответа в видео JSON, иначе выдаст ошибку, поэтому:
        Map<String, String> data = new HashMap<>(); //создаем мапу
        data.put("email", "customer@practicesoftwaretesting.com"); //кладем в нее это
        data.put("password", "welcome0345"); //и это

        var loginResponse = request.post("users/login", RequestOptions.create() //создаем переменную, которая получит ответ на запрос
                .setData(data)
        );
        /*
        while (loginResponse.status() == 401) {
            System.out.println("Статус 401... пробуем еще раз");
            loginResponse = request.post("users/login", RequestOptions.create() //перезаписываем переменную новым ответом
                    .setData(data)
            );
        }
        Альтернатива, чтоб цикл вечно не крутился:

        int attempts = 0; // Наш счетчик
        int maxAttempts = 20; // Предохранитель, чтобы тест не шел вечно
        while (loginResponse.status() == 401 && attempts < maxAttempts) {
            attempts++; // Увеличиваем счетчик на 1
            System.out.println("Попытка №" + attempts + " | Статус всё еще 401...");
            loginResponse = request.post("users/login", RequestOptions.create().setData(data));
        }
        */
        for (int i = 0; i<10; i++) {
            loginResponse = request.post("users/login", RequestOptions.create()
                    .setData(data));
            if (loginResponse.status() == 423) { // Если получили 423 — досрочно выходим из цикла, мы добились цели!
                break;
            }
        }
        assertThat(loginResponse.status()).isEqualTo(423);
        System.out.println("Мы успешно получили статус: " + loginResponse.status() + ", Тело ответа: " + loginResponse.text());
    }

    @Test
    @DisplayName("Create a new user and fail to login once, 401 error check")
    @Story("Login API tests")
    void should_failToLoginNewUser() {
        User newUser = User.randomUser();
        var registerUserResponse = request.post("users/register", RequestOptions.create()
                .setData(newUser)
        );
        assertThat(registerUserResponse.status()).isEqualTo(201);
        Map<String, String> data = new HashMap<>();
        data.put("email", newUser.email());
        data.put("password", "wrongpassword");

        var loginUserResponse = request.post("users/login", RequestOptions.create()
                .setData(data)
        );
        assertThat(loginUserResponse.status()).isEqualTo(401); //пользователь каждый раз новый, поэтому всегда сработает assert
        System.out.println("Статус логина: " + loginUserResponse.status() + ", Тело ответа: " + loginUserResponse.text());
    }
}
