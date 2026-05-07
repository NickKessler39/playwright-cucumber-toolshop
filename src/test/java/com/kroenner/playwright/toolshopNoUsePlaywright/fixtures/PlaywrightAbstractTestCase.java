package com.kroenner.playwright.toolshopNoUsePlaywright.fixtures;

import com.microsoft.playwright.*;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.ScreenshotManager.takeScreenshot;


public abstract class PlaywrightAbstractTestCase {
    //Браузер создается автоматически в тот момент, когда он понадобился первому тесту в потоке. Нам больше не нужно явно вызывать Playwright.create() в @BeforeAll.
    protected static ThreadLocal<Playwright> playwright = //Теперь у каждого потока (класса) свой изолированный драйвер playwright
            ThreadLocal.withInitial(
                    () -> {
                        Playwright playwright = Playwright.create();
                        playwright.selectors().setTestIdAttribute("data-test");
                        return playwright;
                    }
            );
/*
Даже если методы в одном классе идут по очереди, сами классы (файлы) у тебя запускаются параллельно.
Если оставить static Browser browser обычным:
    Класс CartTests запускает браузер.
    Класс LoginTests (в другом потоке) видит, что переменная browser уже заполнена, и пытается в ней создать свой контекст.
    Когда CartTests закончит работу и вызовет browser.close(), он прибьет браузер и для LoginTests, который еще не доделал работу.
Когда мы пишем static ThreadLocal<Browser> browser, мы говорим Java: «Создай переменную, которая будет выглядеть как одна,
но для каждого потока внутри нее будет лежать своё собственное значение».
Лямбда-выражение (() -> { ... }) — это просто инструкция на будущее.
Мы не создаем браузер прямо сейчас. Мы говорим: «Если какой-то поток впервые попросит браузер через метод .get(),
то выполни вот этот код: создай Playwright, запусти Chromium и положи результат в ячейку этого потока».
*/

    protected static ThreadLocal<Browser> browser = //Теперь у каждого потока (класса) свой изолированный браузер
            ThreadLocal.withInitial(
                    () ->
                        playwright.get().chromium().launch(
                                new BrowserType.LaunchOptions().setHeadless(Boolean.parseBoolean(System.getProperty("headless", "true")))
                                        .setArgs(Arrays.asList("--no-sandbox", "--disable-extensions", "--disable-notifications", "--start-maximized"))
                                //.setSlowMo(100)
                        )
                    );
    /*Почему мы убрали static у browserContext и page?
    Потому что в новой схеме JUnit создает отдельный экземпляр тестового класса для каждого теста.
    Убрав static, мы дали каждому тесту свою личную тетрадь (page), которую у него никто не заберет.
    */
    protected BrowserContext browserContext;
    protected Page page;


    @BeforeEach
    void setUpBrowserContext() {
        //Создаем контекст и просим его НЕ фиксировать ВЬЮПОРТ
        //Передавая null, мы говорим: "подстраивайся под размер окна браузера"
        browserContext = browser.get().newContext(new Browser.NewContextOptions() //Чтобы достать «свой» браузер из ячейки ThreadLocal, нужно вызвать .get().
                .setViewportSize(null));
        page = browserContext.newPage();
    }

    @AfterEach
    void closeContext() {
        ScreenshotManager.takeScreenshot(page, "End of test");
        browserContext.close();
    }


    @AfterAll
    static void tearDown() {
        browser.get().close();
        browser.remove();

        playwright.get().close(); //Мы закрываем браузер текущего потока и обязательно очищаем ячейку (remove), чтобы не было утечки памяти.
        playwright.remove();
    }

    public BrowserContext getBrowserContext() { //мост, для того, чтобы передавать контекст браузера для интерфейса WithTracing
        return this.browserContext;
    }
}
