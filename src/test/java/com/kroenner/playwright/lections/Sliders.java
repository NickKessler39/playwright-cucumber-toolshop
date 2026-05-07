package com.kroenner.playwright.lections;

import com.microsoft.playwright.*;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.*;

@UsePlaywright(Sliders.MyOptions.class)

public class Sliders {
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

    @BeforeEach
    void openTheCataloguePage(Page page) {
        openPage(page);
    }

    void openPage(Page page) {
        page.navigate("https://practicesoftwaretesting.com/");
    }


    @DisplayName("Testing price range sliders")
    @Test
    void priceRangeSliderTest(Page page) {
        Locator minSlider = page.getByRole(AriaRole.SLIDER, new Page.GetByRoleOptions().setName("ngx-slider").setExact(true)); //setExact, так как setName неполное совпадение ищет
        Locator maxSlider = page.getByRole(AriaRole.SLIDER, new Page.GetByRoleOptions().setName("ngx-slider-max"));
        assertThat(minSlider).isVisible();
        assertThat(maxSlider).isVisible();

        assertThat(page.getByText("Price Range")).isVisible();
        assertThat(page.locator(".ngx-slider-pointer-max")).isVisible();
        assertThat(page.locator(".ngx-slider-pointer-min")).isVisible();
        assertThat(page.getByRole(AriaRole.SLIDER)).hasCount(2);
        Assertions.assertEquals("1", minSlider.getAttribute("aria-valuenow")); //дефолтное значение minSlider 1

        BoundingBox minSliderBox = minSlider.boundingBox();
        //Playwright запрашивает у браузера точные координаты элемента на экране:
        // x, y: координаты верхнего левого угла.
        // width, height: ширина и высота ползунка.
        minSlider.focus();
        minSlider.hover();
        //Мы перемещаем курсор ровно в центр слайдера MinSlider.
        // x + width / 2 — это середина по горизонтали.
        // y + height / 2 — это середина по вертикали.
        page.mouse().move(minSliderBox.x + minSliderBox.width / 2, minSliderBox.y + minSliderBox.height / 2);
        page.mouse().down(); //зажимаем мышь
        //Тянем курсор вправо (прибавляем 34 пикселя к текущей позиции X)
        page.mouse().move(minSliderBox.x + minSliderBox.width / 2 + 34, minSliderBox.y + minSliderBox.height / 2);
        page.mouse().up(); //отпускаем мышь
        Assertions.assertEquals("30", minSlider.getAttribute("aria-valuenow")); //значение minSlider должно теперь быть 30
        //Max Slider:
        Assertions.assertEquals("100", maxSlider.getAttribute("aria-valuenow"));
        BoundingBox maxSliderBox = maxSlider.boundingBox();
        maxSlider.focus();
        maxSlider.hover();
        page.mouse().move(maxSliderBox.x + maxSliderBox.width / 2, maxSliderBox.y + maxSliderBox.height / 2);
        page.mouse().down();
        page.mouse().move(maxSliderBox.x + maxSliderBox.width / 2 - 51, maxSliderBox.y + maxSliderBox.height / 2);
        page.mouse().up();
        Assertions.assertEquals("55", maxSlider.getAttribute("aria-valuenow"));
    }

    @Test
    @DisplayName("Testing price range")
    void priceRangeTest(Page page) {
        Locator minSlider = page.getByRole(AriaRole.SLIDER, new Page.GetByRoleOptions().setName("ngx-slider").setExact(true)); //setExact, так как setName неполное совпадение ищет
        Locator maxSlider = page.getByRole(AriaRole.SLIDER, new Page.GetByRoleOptions().setName("ngx-slider-max"));
        verifyPricesInRange(page, 1, 100);
        moveSliderTo(page, minSlider, 15);
        assertThat(minSlider).hasAttribute("aria-valuenow", "15");
        moveSliderTo(page, maxSlider, 50);
        assertThat(maxSlider).hasAttribute("aria-valuenow", "50");
        verifyPricesInRange(page, 15, 50);
    }
    private void verifyPricesInRange(Page page, double min, double max) {
        List<String> prices = page.getByTestId("product-price").allTextContents();
        Assertions.assertFalse(prices.isEmpty(), "Список товаров пуст!");    // Проверка, что фильтр не скрыл вообще все товары (защита от "пустой страницы")
        for (String priceText : prices) {
            String cleanPrice = priceText.replace("$", "").trim();
            double priceValue = Double.parseDouble(cleanPrice);
            Assertions.assertTrue(priceValue >= min && priceValue <= max,
                    "Товар с ценой " + priceValue + " не должен быть виден");
        }
    }
    private void moveSliderTo(Page page, Locator slider, int targetValue) {
        // 1. Встаем на ползунок
        BoundingBox box = slider.boundingBox();
        double currentX = box.x + box.width / 2;
        double centerY = box.y + box.height / 2;

        page.mouse().move(currentX, centerY);
        page.mouse().down();

        // 2. Начинаем движение в цикле
        int currentValue = Integer.parseInt(slider.getAttribute("aria-valuenow"));
        int attempts = 0; // Защита от бесконечного цикла

        while (currentValue != targetValue && attempts < 200) {
            if (currentValue < targetValue) {
                currentX += 1; // Шагаем на 1 пиксель вправо
            } else {
                currentX -= 1; // Шагаем на 1 пиксель влево
            }

            page.mouse().move(currentX, centerY);
            currentValue = Integer.parseInt(slider.getAttribute("aria-valuenow"));
            attempts++;
        }
        page.mouse().up();
    }
}
