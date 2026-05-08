package com.kroenner.playwright.lections;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

public class SimplePageObjectTest {
    protected static Playwright playwright;
    protected static Browser browser;
    protected static BrowserContext browserContext;
    Page page;


    @BeforeAll
    static void setUpBrowser() {
        playwright = Playwright.create();
        playwright.selectors().setTestIdAttribute("data-test");
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(Boolean.parseBoolean(System.getProperty("headless", "true"))).
                        setArgs(Arrays.asList("--no-sandbox", "--disable-extensions", "--disable-notifications"))
        );
    }

    @BeforeEach
    void setUpBrowserContext() {
        browserContext = browser.newContext();
        page = browserContext.newPage(); //это команда: "Открыть новую пустую вкладку". С этого момента объект page — это реальное окно в браузере (about:blank)
        page.navigate("https://practicesoftwaretesting.com/"); //Теперь мы говорим этому окну: "Перейди по адресу"
    }

    @AfterEach
    void closeContext() {
        browserContext.close();
    }

    @AfterAll
    static void tearDown() {
        browser.close();
        playwright.close();
    }
    @Nested
    class WhenSearchingProductsByKeyword {

        @DisplayName("Without Page Objects")
        @Test
        void withoutPageObjects() {
            page.waitForResponse("**/products/search?q=tape", () -> {
                        page.getByPlaceholder("Search").fill("tape");
                        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
                    });
            List<String> matchingProducts = page.getByTestId("product-name").allInnerTexts();
            Assertions.assertThat(matchingProducts).contains("Tape Measure 7.5m", "Measuring Tape", "Tape Measure 5m");
        }

        @DisplayName("With Page Objects")
        @Test
        void withPageObjects() {
            // 1. Мы создаем экземпляр (объект) нашего поискового компонента.
            // Мы передаем туда 'page' (нашу активную вкладку браузера),
            // чтобы компонент "ожил" и мог управлять этой вкладкой.
            SearchComponent searchComponent = new SearchComponent(page);
            // 2. Создаем объект для работы со списком продуктов.
            // Опять передаем 'page', чтобы он знал, откуда считывать названия товаров.
            ProductList productList = new ProductList(page);
            // 3. Вызываем метод 'searchBy'. Нам не важно, какой там селектор или API-запрос.
            // Мы просто говорим: "Поиск, найди мне 'tape'".
            searchComponent.searchBy("tape"); //выполняем действия через метод объекта (с маленькой буквы!)
            // 4. Просим объект 'productList' сходить на страницу и собрать все названия.
            // Переменная 'var' сама поймет, что это List<String>.
            var matchingProducts = productList.getProductNames();
            // 5. Финальная проверка: убеждаемся, что в списке есть нужные нам строки.
            Assertions.assertThat(matchingProducts).contains("Tape Measure 7.5m", "Measuring Tape", "Tape Measure 5m");
        }

        class SearchComponent {
            // Это "память" нашего класса. Здесь будет лежать ссылка на вкладку браузера.
            private final Page page; //Это личная память конкретно класса SearchComponent, она пустая.
            // Это КОНСТРУКТОР. Он срабатывает, когда ты пишешь 'new SearchComponent(page)'.
            // Его единственная задача — взять страницу из теста и положить её в "память" выше.
            SearchComponent(Page page) {
                this.page = page; // 'this.page' — это поле класса, 'page' — это то, что пришло снаружи. Сохраняем в пустую приватную память класса Search Component значение page.
                //this.page = page; — это как сказать: «Запомни эту страницу как свою личную». this означает «принадлежащий этому конкретному объекту».
                //Потому что принять значение мало. Как только конструктор доработает, всё, что было в его скобках, как в SearchComponent(Page page), Java «забудет».
                //Чтобы этого не случилось, мы перекладываем значение на склад (в поле класса):
                //this.page = page;
            }
            // Это МЕТОД (Действие).
            public void searchBy(String keyword) {
                // Мы используем 'page' из памяти класса, чтобы отправить команды в браузер.
                page.waitForResponse("**/products/search?q=tape", () -> { //Ждем API ответа, после того как:
                    page.getByPlaceholder("Search").fill("tape"); // Заполняем поле
                    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click(); // Жмем кнопку
                });
            }
            /* Представь, что класс SearchComponent — это телефон. У него есть функция «Звонить» (метод searchBy).
            Но телефон не будет работать без электричества (Page).
            Поле private final Page page: Это гнездо для зарядки внутри телефона. Оно просто есть в конструкции, но пока оно пустое, телефон — кирпич.
            Конструктор SearchComponent(Page page): Это процесс подключения кабеля.
            Когда ты в тесте пишешь new SearchComponent(page), ты берешь «кабель с током» из теста и втыкаешь его в телефон.
            Инструкция { this.page = page; } внутри конструктора — это момент, когда ток пошел в аккумулятор. Теперь телефон «заряжен» и может звонить. */
        }
        class ProductList {
            private final Page page; // Тоже своя "память" для страницы.
            // Конструктор: "Привет, я список продуктов, дай мне страницу, на которой я нахожусь".
            ProductList(Page page) {
                this.page = page;

            }
            // Метод для получения данных. Он не совершает действий, он только возвращает информацию.
                public List<String> getProductNames() {
                    // Идем в браузер, находим все элементы с data-test="product-name"
                    // и превращаем их в список строк.
                return page.getByTestId("product-name").allInnerTexts();
            }
        }
    }
}
