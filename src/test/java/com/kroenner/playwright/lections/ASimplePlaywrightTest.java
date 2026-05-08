package com.kroenner.playwright.lections;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

@UsePlaywright(ASimplePlaywrightTest.MyOptions.class) //просим аннотацию заглянуть в класс MyOptions
public class ASimplePlaywrightTest {

    public static class MyOptions implements OptionsFactory { //обещаем Playwright, что создадим метод getOptions (именно такой!), который вернет все настройки
        //когда пишешь implements OptionsFactory, ты можешь нажать Alt + Enter, и среда сама создаст заготовку метода с правильным названием, чтобы ты не ошибся ни в одной букве

        @Override
        public Options getOptions() { //имлементируем обещанный метод с кастомными настройками
            return new Options()
                    .setHeadless(Boolean.parseBoolean(System.getProperty("headless", "true")))
                    .setLaunchOptions(
                            new BrowserType.LaunchOptions()
                                    .setArgs(Arrays.asList("--disable-extensions", "--disable-notifications"))
                    );
        }
    }

    @Test
    void shouldShowThePageTitle(Page page) {
        page.navigate("https://practicesoftwaretesting.com/");
        String title = page.title();
        Assertions.assertTrue(title.contains("Practice Software Testing"));
    }

    @Test
    void shouldSearchByKeyword(Page page){
        page.navigate("https://practicesoftwaretesting.com/");
        page.locator("[placeholder=Search]").fill("Pliers");
        page.locator("button:has-text('Search')").click();

        int matchingSearchResults = page.locator(".card").count();
        Assertions.assertTrue(matchingSearchResults > 0);
    }
}
