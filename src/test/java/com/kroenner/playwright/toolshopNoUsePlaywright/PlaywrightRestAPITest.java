package com.kroenner.playwright.toolshopNoUsePlaywright;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kroenner.playwright.toolshopNoUsePlaywright.catalog.pageobjects.NavBar;
import com.kroenner.playwright.toolshopNoUsePlaywright.catalog.pageobjects.ProductList;
import com.kroenner.playwright.toolshopNoUsePlaywright.catalog.pageobjects.SearchComponent;
import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.PlaywrightAbstractTestCase;
import com.kroenner.playwright.toolshopNoUsePlaywright.mocking.MockSearchResponses;
import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.LoadState;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.stream.Stream;

import static java.nio.file.Files.isHidden;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Search Tests")
@Feature("Search")
public class PlaywrightRestAPITest extends PlaywrightAbstractTestCase {
    SearchComponent searchComponent;
    ProductList productList;
    NavBar navBar;

    @BeforeEach
    void openTheCataloguePage() {
        setUp();
        openPage();
    }

    void openPage() {
        navBar.openHomePage();
    }

    void setUp() {
        searchComponent = new SearchComponent(page);
        productList = new ProductList(page);
        navBar = new NavBar(page);
    }

    @Nested
    @DisplayName("Search Tests")
    @Story("Search API Tests")
    class MakingAPICalls {
        record Product(String name, Double price) {
        }

        //В обычном тесте ты используешь Page (это твой «глаз» в браузере).
        private static APIRequestContext requestContext; //В API-тесте браузер не нужен, поэтому мы используем APIRequestContext.
        //Это объект-клиент, который умеет отправлять HTTP-запросы (GET, POST, PUT, DELETE) и получать ответы от сервера.
        //Он заменяет собой целую вкладку браузера, но работает «под капотом».


        @BeforeAll
        public static void setupRequestContext() {
            requestContext = playwright.get().request().newContext(
                    new APIRequest.NewContextOptions()
                            .setBaseURL("https://api.practicesoftwaretesting.com") //Теперь в тестах тебе не нужно писать длинный адрес.
                            //Ты будешь писать просто /products, а Playwright сам подставит в начало https://api.practicesoftwaretesting.com.
                            .setExtraHTTPHeaders(new HashMap<>() {{ //Это «паспорт» твоего запроса.
                                //Заголовок Accept: application/json говорит серверу: «Эй, я понимаю только формат JSON, отвечай мне в нем».
                                put("Accept", "application/json");
                            }})
            );
        }

        @DisplayName("Check presence of known produсts")
        @Story("Search API Tests")
        @ParameterizedTest(name = "Checking product {0}")
        //Это магия JUnit 5. Она говорит: «Не запускай этот тест один раз. Запусти его столько раз, сколько объектов придет из источника данных».
        //name = "Checking product {0}": Это шаблон для отображения каждого запуска.
        //{0}: Это подстановка первого аргумента (в нашем случае это объект product в checkKnownProduct, который содержит все данные).
        @MethodSource("products")
            //Когда JUnit видит аннотацию @MethodSource("products"), он находит метод с таким именем, запускает его один раз и забирает из него «поток» (Stream) данных.
            //Это «пуповина», которая соединяет тест с данными.
            //JUnit ищет в этом же классе метод с названием products.
            //Он видит твой метод static Stream<Product> products(), который мы разбирали раньше.
            // Он «вытягивает» из стрима по одному товару и передает его в аргумент void checkKnownProduct(Product product).
        void checkKnownProduct(Product product) {
            page.fill("[placeholder='Search']", product.name()); //заполняем поле с аттрибутом placeholder с помощью значения name из API запроса
            // page.getByPlaceholder("Search").fill(product.name); //то же самое
            page.click("button:has-text('Search')");
            page.waitForLoadState(LoadState.NETWORKIDLE); // Ждем, пока API поиска вернет ответ и страница обновится
            //Далее мы вводим объект типа Locator с названием productCard, которому будет присваиваться значение веб-элемента класса card,
            //но только если он пройдет фильтр "текст взятый из product.name" (нашего API запроса), а также "текст, взятый из product.price",
            //а затем делаем PlaywrightAssert, что получившийся объект типа Locator виден
            Locator productCard = page.locator(".card")
                    .filter(
                            new Locator.FilterOptions()
                                    .setHasText(product.name())
                                    .setHasText(Double.toString(product.price()))
                    );
            PlaywrightAssertions.assertThat(productCard).isVisible();
        }

        static Stream<Product> products() {
            APIResponse response = requestContext.get("/products?page=2"); //Мы просим сервер: «Дай мне вторую страницу списка товаров» (page=2).
            assertThat(response.status()).isEqualTo(200); //Сразу проверяем статус 200 OK. Если сервер упал (500) или страница не найдена (404), дальше идти нет смысла.
//Сервер присылает ответ в виде одной длинной текстовой строки. Работать с ней как со строкой неудобно. Поэтому мы используем библиотеку Gson:
            JsonObject jsonObject = new Gson().fromJson(response.text(), JsonObject.class); //fromJson: Превращает текст в дерево объектов.
            JsonArray data = jsonObject.getAsJsonArray("data"); //В API Toolshop товары лежат не в корне, а внутри ключа "data": [...]. Мы достаем именно этот массив.
//Мы превращаем "сырой" JSON в красивые объекты твоего record Product.
            return data.asList().stream() // Берем список элементов из JSON
                    .map(jsonElement -> { // Для каждого элемента (товара) в списке...
                        JsonObject productJson = jsonElement.getAsJsonObject(); //«Я точно знаю, что внутри этой "коробки" лежит объект (со своими полями внутри, типа name и price),
                        //а не просто одиночная цифра. Распакуй её как объект, чтобы я мог обращаться к её внутренностям».
                        return new Product( // ...создаем новый объект нашего Record Product
                                productJson.get("name").getAsString(), // Достаем имя
                                productJson.get("price").getAsDouble() // Достаем цену
                        );
                    });
        }
    }

    @DisplayName("Mocking tests")
    @Story("Search API Tests")
    @Nested
    class MockingAPIResponses {
        /*В начале тестового метода мы добавляем некую "ловушку", которая будет ждать от сервера во время исполнения теста,
        запрос /products/search?q=Pliers и как только такой запрос будет найдет, то его ответ подменят с помощью класса MockSearchResponses,
        в котором есть новое содержимое ответа в, к примеру, RESPONSE_WITH_A_SINGLE_ENTRY. При этом еще и ставится статус 200, мол, все нормально.
        Затем тест заходит на сайт, в поле поиска вводит Pliers, нажимает энрен и тут как раз ловушка срабатывает,
        наш тест подменяет ответ и тут же быстро проверяет ожидаемый результат, который в случае whenASingleItemIsFound будет в виде одного продукта,
        содержащего текст "Super Pliers" (этот текст есть в теле RESPONSE_WITH_A_SINGLE_ENTRY).
        Ну а в whenNoItemsAreFound() ответ просто пустой, потому что в теле RESPONSE_WITH_NO_ENTRIES ничего нет. Ну точнее, ничего нет в теле поля "data"
        */
        @Test
        @DisplayName("Search for pliers, but find only 1 mocking result")
        @Story("Search API Tests")
        void whenASingleItemIsFound() {
            //Ловушка ставится до навигации или действий (Maps, fill), потому что как только ты нажмешь Enter, браузер мгновенно выстрелит запросом.
            page.route("**/products/search?q=Pliers", route -> {
                System.out.println("Ловушка");
                route.fulfill(
                        new Route.FulfillOptions()
                                .setBody(MockSearchResponses.RESPONSE_WITH_A_SINGLE_ENTRY)
                                .setStatus(200)
                );
            });
            /* По поводу лямбда выражений ->
            Представь, что ты ставишь ловушку в лесу:
            Ты вешаешь табличку: «Если увидишь волка (route), -> накорми его этой котлетой (fulfill)».
            Сама лямбда route -> { ... } — это текст на этой табличке.
            Код внутри { ... } сработает только в тот момент, когда «волк» (запрос) реально прибежит.
            Порядок:
            Сначала отрабатывает page.route (мы просто вешаем табличку-инструкцию).
            Потом идет page.navigate (мы идем в лес).
            Потом fill и press("Enter") (мы создаем шум, на который прибегает «волк»).
            И вот только в момент нажатия Enter Плейрайт «вспоминает» про твою лямбду и выполняет код внутри неё.
            Как читать стрелочку -> проще?
            Читай её как слово «значит» или «сделай следующее»:
            route -> { ... } = «Берем объект запроса (route) и с ним делаем следующее...»
            */
            page.navigate("https://practicesoftwaretesting.com/");
            page.getByPlaceholder("Search").fill("Pliers");
            page.getByPlaceholder("Search").press("Enter");

            PlaywrightAssertions.assertThat(page.getByTestId("product-name")).hasCount(1);
            PlaywrightAssertions.assertThat(page.getByTestId("product-name")).hasText("Super Pliers");

        }

        @Test
        @DisplayName("Search for pliers, but find no result due to mocking")
        @Story("Search API Tests")
        void whenNoItemsAreFound() {
            page.route("**/products/search?q=Pliers", route -> {
                route.fulfill(
                        new Route.FulfillOptions()
                                .setBody(MockSearchResponses.RESPONSE_WITH_NO_ENTRIES)
                                .setStatus(200)
                );
            });
            page.navigate("https://practicesoftwaretesting.com/");
            page.getByPlaceholder("Search").fill("Pliers");
            page.getByPlaceholder("Search").press("Enter");

            PlaywrightAssertions.assertThat(page.getByTestId("product-name")).hasCount(0);
            PlaywrightAssertions.assertThat(
                    page.getByTestId("product-name")
                            .filter(new Locator.FilterOptions().setHasText("Pliers"))
            ).not().isVisible();
            PlaywrightAssertions.assertThat(page.getByTestId("search_completed")).hasText("There are no products found.");
        }
    }
}
