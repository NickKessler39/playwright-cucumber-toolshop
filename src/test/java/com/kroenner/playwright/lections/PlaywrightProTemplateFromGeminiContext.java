package com.kroenner.playwright.lections;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

public class PlaywrightProTemplateFromGeminiContext {
    // 1. Статические ресурсы (одни на весь класс)
    private static Playwright playwright;
    private static Browser browser;

    // 2. Ресурсы инстанса (свои для каждого теста)
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(Boolean.parseBoolean(System.getProperty("headless", "true")))
                .setSlowMo(500)); // Чтобы ты успевал видеть происходящее
    }

    @BeforeEach
    void createContext() {
        // Создаем СВОЙ чистый мир для каждого теста
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    void myFirstTest() {
        page.navigate("https://example.com");
        // Твои действия...
    }

    @AfterEach
    void closeContext() {
        // Убираем за собой "комнату", не трогая "здание"
        if (context != null) context.close();
    }

    @AfterAll
    static void closeBrowser() {
        // Сносим "здание" в самом конце
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}
