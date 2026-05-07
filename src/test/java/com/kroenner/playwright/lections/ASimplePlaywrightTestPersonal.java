package com.kroenner.playwright.lections;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

//@UsePlaywright //Позволяет сократить код, у каждого теста в этом классе буду автоматом прогоняться типичные стартеры и финишеры



public class ASimplePlaywrightTestPersonal {
    //@UsePlaywright (должен быть объявлен снаружи класса) заменяется вот этот кусок кода:
    Playwright playwright;   //Внутри класса, но перед методами, объявляем объекты
    Browser browser;
    Page page;

    @BeforeEach
    void setup() {
        playwright = Playwright.create();  //create environment, иницируем объект Playwright
        browser = playwright.chromium().launch( //open browser, иницируем объект Browser
                new BrowserType.LaunchOptions() //добавляем LaunchOptions в playwright.chromium().launch()
                        .setHeadless(Boolean.parseBoolean(System.getProperty("headless", "true")))
                        .setArgs(Arrays.asList("--disable-extensions", "--disable-notifications"))
        );
        page = browser.newPage(); //create a page, иницируем объект Page
    }
    /* Другие полезные LaunchOptions
       .setSlowMo(500)  //замедляет каждое действие на 500 миллисекунд (полсекунды)
       .setDevtools(boolean) //браузер будет автоматически открываться вместе с панелью разработчика
       Другие полезные setArgs(Arrays.asList()): (для разных браузеров могут отличаться, эти для Chrome)
       --start-maximized //окно будет растянуто на весь экран
       --window-size=1920,1080 //задаем размер окна
       --ignore-certificate-errors //игнорирует предупреждения SSL, тест пойдет дальше, даже если сертификат просрочен или не валиден
       --disable-web-security //отключаем Same-Origin (CORS), это полезно, если тест должен делать запросы между разными доменами
       --disable-gpu //отключает использование видеокарты, стандарт для запуска в Linux-контейнерах
       --no-sandbox //отключает песочницу Chrome, часто необходимо для работы внутри Docker, так как там ограниченные права пользователя
       --disable-dev-shm-usage //заставляет браузер использовать память /tmp вместо /dev/shm, это предотвращает падение браузера из-за нехватки памяти в контейнерах
       --incognito -режим инкогнито
       --disable-notifications //отключает всплывающие окна с вопросом «Разрешить сайту присылать вам уведомления?»
     */

    @AfterEach
    void teardown() {
        browser.close(); //close browser
        playwright.close(); //shutdown browser
    }

    @Test
    void testGeminiTitle() { //при включенном @UsePlaywright здесь надо объявлять объект (Page page)
        page.navigate("https://practicesoftwaretesting.com/");
        page.locator("[title='Practice Software Testing - Toolshop']").isVisible(); //виден ли этот элемент, важны '' кавычки, иначе из-за пробелов упадет
        assertThat(page.locator("[title='Practice Software Testing - Toolshop']")).isVisible(); //тоже самое, но "более устойчивый" вариант
        assertThat(page).hasTitle(Pattern.compile("Practice Software Testing")); //проверяет, что title равняется Practice Software Testing + любые символы после
    }

    @Test
    void testGeminiLogo() { //при включенном @UsePlaywright здесь надо объявлять объект (Page page)
        page.navigate("https://practicesoftwaretesting.com/");
        page.locator("[class=gear]").isVisible(); //видна ли шестеренка
        assertThat(page.locator("[class=st3]")).hasCount(5); //первые 4 буквы-картинки логотипа, слово TOOL + шестеренка
        assertThat(page.locator("[class=st4]")).hasCount(4); //последние 4 буквы-картинки логотипа, слово SHOP

        Locator logo = page.locator("a.navbar-brand"); //находим класс a class="navbar-brand"
        assertThat(logo).isVisible(); //проверяем, что этот класс виден
        assertThat(logo.locator("svg")).isVisible(); //проверяем, есть ли в нем хотя бы один элемент с тегом svg
                                                                    //(там есть целый svg id="Layer 1", в котором и есть все картинки логотипа)
        Locator letters = page.locator("a.navbar-brand svg path"); //обозначаем все элементы внутри svg
        Assertions.assertEquals(14, letters.count(), "Количество элементов в логотипе не совпадает!"); //
    }

    @Test
    void testGeminiSearch() { //при включенном @UsePlaywright здесь надо объявлять объект (Page page)
        page.navigate("https://practicesoftwaretesting.com/");
        page.locator("[id=search-query]").fill("Hammer"); //вводим "Hammer" в поле c id = search-query
        //page.locator("[class='btn btn-secondary']").click(); //кликаем кнопку "Search", этот вариант ищет точное совпадение по 'btn btn-secondary'
        page.locator("button:has-text('Search')").click(); //вариант преподавателя, ищем кнопку с текстом "Search" где угодно
        //page.locator("button >> text=Search").click();  //более строгий вариант преподавателя, ищет точное значение
        //page.locator("button[data-test='search-submit']").click(); //вариант Gemini, ищем кнопку с атрибутом data-test='search-submit'
        //page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click(); //вариант от Playwright

        Locator cards = page.locator("a.card"); //обозначаем элементы a class="card"
        //Assertions.assertEquals(7, cards.count()); //считаем их количество и сравниваем с ожидаемым значением
        //Лучше использовать assertThat, так как он будет ждать 5 секунд, пока количество элементов не станет равным 7
        assertThat(cards).hasCount(7);
    }

    @Test
    void testGeminiSearchAnItem() {
        page.navigate("https://practicesoftwaretesting.com/");
        page.locator("[id=search-query]").fill("Hammer"); //технический уровень локатора, ищем по конкретном id и заполняем поле словом "Hammer"
        page.locator("button:has-text('Search')").click(); //функциональный уровень локатора от Playwright, ищем кнопку с текстом Search
        //page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Thor Hammer")).click(); //семантический уровень локатора от Playwright, ищем элемент с ролью LINK
                                                                                                     //у которого есть текст "Thor Hammer"
        //page.locator("a.card >> text=Thor Hammer").click(); //ищем элемент класса a.card с текстом Thor Hammer
        page.locator("a.card").getByText("Thor Hammer").click(); //семантический вариант предыдущего
        //page.getByAltText("Thor Hammer").click(); //так как товар имеет атрибут alt="Thor Hammer", можно взять использовать такую семантику
        assertThat(page.locator("[data-test=product-name]")).hasText("Thor Hammer");
    }

}
