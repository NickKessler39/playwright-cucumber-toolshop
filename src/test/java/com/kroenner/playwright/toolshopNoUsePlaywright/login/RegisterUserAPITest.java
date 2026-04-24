package com.kroenner.playwright.toolshopNoUsePlaywright.login;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kroenner.playwright.toolshopNoUsePlaywright.domain.User;
import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.PlaywrightAbstractTestCase;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.options.RequestOptions;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Login Tests")
@Feature("Login")
public class RegisterUserAPITest extends PlaywrightAbstractTestCase {

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
    @DisplayName("Create a new user")
    @Story("Registration API tests")
    void should_register_user() {
        User validUser = User.randomUser();

        var response = request.post("/users/register", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                //Content-Type: Это заголовок, который говорит серверу: "Эй, я сейчас пришлю тебе данные, и они будут в формате JSON".
                //application/json: Это стандартное значение (MIME-type).
                //.setHeader("Content-Type", "application/json") // Ключ — тип, Значение — json
                //.setHeader("Authorization", "Bearer ABC123Token") // Ключ — авторизация, Значение — токен
                //Если ты его не укажешь или напишешь там text/plain, сервер может просто не понять, как читать твой запрос, и выдаст ошибку (часто 415 Unsupported Media Type или 422).
                .setData(validUser) //передаем объект record User, сгенерированный в User validUser = User.randomUser();
        );

        String responseBody = response.text();
        Gson gson = new Gson();
        User createdUser = gson.fromJson(responseBody, User.class); //«Gson, возьми этот текст и разложи его по полочкам в мой record User»

        JsonObject responseObject = gson.fromJson(responseBody, JsonObject.class); //забираем из ответа от API тело (String responseBody = response.text();),
        //а получившееся будет объект класса JsonObject, который можно использовать, чтоб из него что-то достать
        System.out.println("Id of the new user is " + responseObject.get("id").getAsString());


        SoftAssertions.assertSoftly(softly -> { //вместо assertThat(response.status()).isEqualTo(201);
            softly.assertThat(response.status())
                    .as("Registration should return 201 created status")
                    .isEqualTo(201);
            softly.assertThat(createdUser) //вместо assertThat(createdUser).isEqualTo(validUser.withPassword(null));
                    .as("Created user should match the specified user without the password")
                    .isEqualTo(validUser.withPassword(null));

            softly.assertThat(responseObject.has("password")) //проверяем, что объект класса JsonObject по имени responseObject
                    .as("No password should be returned") //сообщение об ошибке, если ассерт провалится
                    .isFalse(); //НЕ должен возвращать пароль (API ответ не возвращает пароль)

            softly.assertThat(responseObject.get("id").getAsString())
                    .as("Registered user should have an id")
                    .isNotEmpty();

            softly.assertThat(response.headers().get("content-type"))
                    .as("as")
                    .contains("application/json");
        });
        //Работает как судья, который записывает все нарушения в блокнот, но дает доиграть матч до конца.
        // Если статус не 201, тест не остановится.
        // Он пойдет дальше, выполнит все остальные проверки внутри блока assertSoftly, и только в самом конце вывалит тебе список всех найденных несоответствий.

        User passwordlessUser = User.randomUser().withPassword("");
        var responseWithoutPassword = request.post("/users/register", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(passwordlessUser));
        assertThat(responseWithoutPassword.status()).isEqualTo(422);

        // 1. Создали и зарегистрировали (это ты уже умеешь)
        User user = User.randomUser();
        request.post("/users/register", RequestOptions.create().setData(user));

// 2. Сразу пытаемся войти под ним
        String loginBody = "{\"email\":\"" + user.email() + "\",\"password\":\"" + user.password() + "\"}";

        var loginResponse = request.post("/users/login", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(loginBody));

        System.out.println("Статус логина: " + loginResponse.status());
    }

    @Test
    @DisplayName("Fail to register a user without first name")
    @Story("Registration API tests")
    void first_name_is_mandatory() {
        User baseUSer = User.randomUser();
        User userWithoutName = new User(
                null,
                baseUSer.last_name(),
                baseUSer.address(),
                baseUSer.phone(),
                baseUSer.dob(),
                baseUSer.email(),
                "ABC123!22"
        );
        var response = request.post("/users/register", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(userWithoutName));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.status())
                    .as("Registration should return 201 created status")
                    .isEqualTo(422);
            softly.assertThat(response.text()).contains("The first name field is required.");
//Далее: мы создаем объект responseObject класса JsonObject,
//делаем мы это с помощью инициализации объекта gson класса Gson,
//которому говорим: "возьми response, десериализуй (добудь) из него текст и переведи его в JsonObject"
//Нюанс: Java получает в response.text() лишь текст, который выглядит как JSON, а нам нужен объект, с которым она сможет работать.
//Когда ты говоришь: «Мы получаем JSON от API», ты прав наполовину.
//Сервер действительно присылает данные в формате JSON, но для твоего кода на Java этот «JSON» приходит в виде обычной строки (String).
//Если ты хочешь достать текст ошибки из обычной строки, тебе пришлось бы делать что-то ужасное:
//String error = response.text().substring(20, 45); // Вырезать кусочек текста по номеру символа. Это сломается при любом изменении!
//C Gson ты превращаешь «тупую» строку в «умный» объект. Теперь Java понимает структуру.
//responseObject.get("first_name") // Ты обращаешься к полю по имени, и Java сама находит его в этой куче текста.

            Gson gson = new Gson(); //создаем и инициируем объект gson, который служит переводчиком между Java и API, в данном случае:
            JsonObject responseObject = gson.fromJson(response.text(), JsonObject.class); //берет из тела API-ответа текст и превращает в Json объект,
                                                                                          //понятного Java, теперь она понимает, где в этом тексте ключи, а где значения
            softly.assertThat(responseObject.has("first_name")).isTrue(); //с которым мы тут проверяем, что у него есть поле "first_name"
            String errorMessage = responseObject.get("first_name").getAsString(); //тут создаем сообщением об ошибке, забирая его из ключа first_name тела responseObject и превращай в String
            assertThat(errorMessage).isEqualTo("The first name field is required."); //сравниваем их
        });
    }
}
