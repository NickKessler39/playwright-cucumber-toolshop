package com.kroenner.playwright.lections;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@UsePlaywright(WaitingForAPICalls.MyOptions.class)

public class WaitingForAPICalls {
    public static class MyOptions implements OptionsFactory {
        @Override
        public Options getOptions() {
            return new Options()
                    .setHeadless(Boolean.parseBoolean(System.getProperty("headless", "true")))
                    .setTestIdAttribute("data-test")
                    .setLaunchOptions(new BrowserType.LaunchOptions()
                            .setArgs(Arrays.asList("--disable extensions", "--disable-notifications"))
                            .setSlowMo(100)
                    );

        }
    }

    @Nested //Делаем тестсьют, со своими BeforeEach
    class WaitingForState {
        String mainPage = "https://practicesoftwaretesting.com/"; //может использоваться всеми тестами из Nested набора

        @BeforeEach
        void openTheCataloguePage(Page page) {
            openPage(page);
            page.waitForSelector(".card-img-top"); //есть ли элементы с классом .card-img-top?
        }
        void openPage(Page page) {
            page.navigate(mainPage);
        }


        @Test
        void sortByDescendingPrice(Page page) {
            //https://api.practicesoftwaretesting.com/products?page=0&sort=price,desc&between=price,1,100&is_rental=false
           page.waitForResponse("**/products*sort=price,desc**", //взяли из Network браузера API запрос выше, выбрали его кусок и обернули в wildcard **
                   () -> { //жди ответ от этого API запроса, когда:
               page.getByTestId("sort").selectOption("Price (High - Low)"); //происходит выбор сортировки (это то, что триггерит этот API запрос)
               page.getByTestId("product-price").first().waitFor(); //в принципе не нужно
                    });

            var productPrices = page.getByTestId("product-price") //Найди все элементы, у которых id равен product-price
                    .allInnerTexts() //Это команда Playwright. Он идет в браузер, собирает текст из всех найденных цен и возвращает нам List<String>.
                    .stream() //конвейер, по которому каждый элемент будет обрабатываться
                    .map(WaitingForState::extractPrice) //Возьми каждый элемент с конвейера и преврати его в другой, например:
                    //Берется строка "$15.00".
                    //Отправляется в метод extractPrice.
                    //Там превращается в число 15.0.
                    //Число едет дальше по конвейеру.
                    .toList(); //Конвейер, стоп! Собери всё, что получилось (числа Double), и сложи обратно в новый список
            //На выходе имеем: List<Double> со значениями типа [15.0, 12.99, 9.5].

            System.out.println("ProductPrices: " + productPrices);
            Assertions.assertThat(productPrices)
                    .isNotEmpty()
                    .isSortedAccordingTo(Comparator.reverseOrder());
        }

        private static double extractPrice(String price) {
            return Double.parseDouble(price.replace("$", ""));
        }
     }
}
