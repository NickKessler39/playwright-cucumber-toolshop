package com.kroenner.playwright.toolshopNoUsePlaywright.cucumber.stepdefinitions;

import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.ScreenshotManager;
import com.microsoft.playwright.*;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;

import java.util.Arrays;

public class PlaywrightCucumberFixtures {
    //final запрещает перезаписывать контейнер с объектом, но разрешает класть и доставать значения внутри него
    //например с помощью get() или set()
    private static final ThreadLocal<Playwright> playwright =
            ThreadLocal.withInitial(
                    () -> {
                        Playwright playwright = Playwright.create();
                        playwright.selectors().setTestIdAttribute("data-test");
                        return playwright;
                    }
            );


    private static final ThreadLocal<Browser> browser =
            ThreadLocal.withInitial(
                    () ->
                            playwright.get().chromium().launch(
                                    new BrowserType.LaunchOptions().setHeadless(true)
                                            .setArgs(Arrays.asList("--no-sandbox", "--disable-extensions", "--disable-notifications", "--start-maximized"))
                                    //.setSlowMo(100)
                            )
            );
    //В Cucumber шаги (Given, When, Then) могут быть раскиданы по разным Java-классам.
    //Чтобы не передавать объект Page из рук в руки через конструкторы (что бывает громоздко), мы делаем его статическим в ThreadLocal.
    //Теперь в любом классе ты просто пишешь driver.getPage() — и Java "магически" понимает:
    // "Ага, это Поток №1 спрашивает, выдам-ка я ему именно ту страницу, которую он открыл в методе @Before".

    private static final ThreadLocal<BrowserContext> browserContext = new ThreadLocal<>();
    private static final ThreadLocal<Page> page = new ThreadLocal<>();
    //аннотация Cucumber
    @Before(order = 100) //прописываем порядок исполнения, чтоб не было конфликтов типа "страница из Fixtures еще не создалась,
    // но тест ее пытается дернуть и валится, потому что page is null
    //Дефолтное значения порядка - 1000, поэтому 100 уже заставит этот метод первым запускаться
    public void setUpBrowserContext() { //методы должны быть public, иначе Cucumber не увидит их
        browserContext.set(browser.get().newContext(new Browser.NewContextOptions().setViewportSize(null)));
        //ThreadLocal — это не обычная переменная, а «контейнер».
        //Ты больше не можешь просто положить в него значение через равно (=) или достать его напрямую.
        //.set(value) — «положить значение в сейф текущего потока».
        //.get() — «достать значение из сейфа текущего потока».
        page.set(browserContext.get().newPage());
        //browserContext.get() - Сначала мы идем в «сейф» контекста браузера и говорим: «Дай мне контекст, который принадлежит именно этому потоку».
        //.newPage() - У этого контекста мы вызываем стандартный метод Playwright для создания страницы.
        //page.set(...): Полученную страницу мы бережно кладем в «сейф» для страниц (ThreadLocal<Page>), чтобы потом любой другой класс мог её оттуда достать.

    }

    @After //аннотация Cucumber
    public void closeContext() {
        //метод требует объект Page, а получает контейнер с Page,
        ScreenshotManager.takeScreenshot(page.get(), "End of test"); //а теперь его надо распаковать с помощью page.get()
        browserContext.get().close();
    }


    @AfterAll //аннотация Cucumber
    public static void tearDown() {
        browser.get().close();
        browser.remove();

        playwright.get().close();
        playwright.remove();
    }
/*
Теперь во всех твоих Step Definitions, когда ты захочешь кликнуть по кнопке, ты не сможешь написать:
page.click("#button") — это выдаст ошибку.
Тебе придется писать:
page.get().click("#button")
Совет: Чтобы не писать этот бесконечный .get() в каждом шаге, часто делают вспомогательный метод:
 */
    public static Page getPage() {
        return page.get();
    }
    //Если метод static, тебе не нужно создавать экземпляр класса PlaywrightCucumberFixtures каждый раз, когда ты хочешь кликнуть по кнопке.
    //Без static: Тебе пришлось бы писать new PlaywrightCucumberFixtures().getPage().click().
    //Со static: Ты просто пишешь PlaywrightCucumberFixtures.getPage().click().

    public static BrowserContext getBrowserContext() {
        return browserContext.get();
    }
}
