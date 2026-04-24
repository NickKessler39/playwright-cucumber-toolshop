package com.kroenner.playwright.toolshopNoUsePlaywright.fixtures;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;


public abstract class PlaywrightAbstractTestCaseNoParallel {

    protected static Playwright playwright;
    protected static Browser browser;
    protected static BrowserContext browserContext;

    protected static Page page;


    @BeforeAll
    static void setUpBrowser() {
        playwright = Playwright.create();
        playwright.selectors().setTestIdAttribute("data-test");
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false)
                        .setArgs(Arrays.asList("--no-sandbox", "--disable-extensions", "--disable-notifications", "--start-maximized"))
                        //.setSlowMo(100)
        );
    }

    @BeforeEach
    void setUpBrowserContext() {
        //Создаем контекст и просим его НЕ фиксировать ВЬЮПОРТ
        //Передавая null, мы говорим: "подстраивайся под размер окна браузера"
        browserContext = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(null));
        page = browserContext.newPage();
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


}
