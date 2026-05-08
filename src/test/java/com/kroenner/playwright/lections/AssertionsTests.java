package com.kroenner.playwright.lections;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import com.microsoft.playwright.options.LoadState;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@UsePlaywright(AssertionsTests.MyOptions.class)

public class AssertionsTests {
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


    @DisplayName("Testing assertions in fields")
    @Test
    void fieldValues(Page page) {

        page.navigate("https://practicesoftwaretesting.com/contact");

        var firstNameField = page.getByLabel("First name");
        firstNameField.fill("Sarah-Jane");
        assertThat(firstNameField).hasValue("Sarah-Jane");

        var lastNameField = page.getByTestId("last-name"); //у нас определен кастомный ID через setTestIdAttribute("data-test"), поэтому id=last_name будет игнорироваться
        lastNameField.fill("Kessler");
        assertThat(lastNameField).hasValue("Kessler");

        var emailAddress = page.getByPlaceholder("Your email");
        emailAddress.fill("kessler@mail.com");
        assertThat(emailAddress).hasValue("kessler@mail.com");

        var messageBox = page.locator("#message");
        messageBox.fill("Hello world!");
        assertThat(messageBox).hasValue("Hello world!");
        assertThat(messageBox).isEnabled();
        assertThat(messageBox).not().isDisabled();
        assertThat(messageBox).isVisible();
        assertThat(messageBox).isEditable();
    }

    @DisplayName("Testing assertions in prices")
    @Test
    void allProductPricesShouldBeCorrectValues(Page page) {
        page.waitForCondition(() -> page.getByTestId("product-name").count() > 0); //ждем, чтоб продукты появились
        List<Double> prices = page.getByTestId("product-price")
                .allInnerTexts() //собираем текст элементов с id "product-price" в виде List<String>
                .stream() //Открывает поток. Теперь мы работаем с каждым элементом по очереди из списка List<String>.
                .map(price -> Double.parseDouble(price.replace("$", ""))) //удаляем лишние символы пустым местом, тип данных меняется с String на Double
                .toList(); //Сохраняем всё в List<Double>

        /* Как бы без stream() выглядело:
        List<String> rawPrices = page.getByTestId("product-price").allInnerTexts(); - создаем список String, собираем в него содержимое элементов product-price
        List<Double> prices = new ArrayList<>(); - создаем пустой список Double (точные значения, которые могут содержать не целые числа)
        for (String price : rawPrices) { //каждый элемент rawPrices становится String price и для каждого String элемента price из rawPrices делаем следующее:
        String cleanPrice = price.replace("$", ""); //создаем String cleanPrice, в котором каждый String price теряет знак $ (он заменяется пустым местом)
        Double doublePrice = Double.parseDouble(cleanPrice); //создаем Double doublePrice, в котором каждый String cleanPrice превращается в тип Double
        prices.add(doublePrice); //в ранее созданный список List<Double> prices добавляем получившийся doublePrice
        */
        Assertions.assertThat(prices) //тут мы используем org.assertj.core.api.Assertions;, а не org.junit.jupiter.api.Assertions;
                .isNotEmpty()
                .allMatch(price -> price > 0) //каждый элемент price должен быть > 0
                .doesNotContain(0.0)
                .allMatch(price -> price < 1000)
                .allSatisfy(price ->
                        Assertions.assertThat(price).isGreaterThan(0.0)
                        .isLessThan(1000.0));

    }

    @DisplayName("Testing alphabetical order")
    @Test
    void shouldSortInAlphabeticalOrder(Page page) {
        page.waitForCondition(() -> page.getByTestId("product-name").count() > 0);

        var sortMenu = page.getByTestId("sort");
        sortMenu.selectOption("name,asc");
        assertThat(sortMenu).hasValue("name,asc");
        page.waitForLoadState(LoadState.NETWORKIDLE); //заставляет Playwright замереть и ничего не делать до тех пор, пока сетевая активность браузера не утихнет

        List<String> actualNames = page.getByTestId("product-name") //создаем список actualNames из всех элементов с id "product-name"
                .allInnerTexts();
        List<String> expectedNames = new ArrayList<>(actualNames); //создаем копию под названием expectedNames
        //List.copyOf(actualNames) тоже создает копию, но ее нельзя менять, а нам надо ее будет сортировать, так что не подходит
        //List<String> expectedNames = actualNames.stream().sorted().toList(); //в принципе хорошая альтернатива, которая уже сразу отсортируется
        Collections.sort(expectedNames); //сортируем эту копию
        //expectedNames.sort(String.CASE_INSENSITIVE_ORDER); - другой вид сортировки, потому что Collections.sort сортирует сначала цифры, потом Заглавные буквы, потом строчные.
        Assertions.assertThat(actualNames).isEqualTo(expectedNames); //сравниваем список actualNames и expectedNames
        Assertions.assertThat(actualNames).isSortedAccordingTo(Comparator.naturalOrder());
        Assertions.assertThat(actualNames).isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER);

        String firstProductBefore = actualNames.get(0); //cохраняем имя первого товара ПЕРЕД сортировкой
        sortMenu.selectOption("name,desc");
        page.waitForCondition(() -> !page.getByTestId("product-name").first().innerText().equals(firstProductBefore)); //ждем, что первый товар в списке поменялся
        page.waitForLoadState(LoadState.NETWORKIDLE); //не помогает, слишком быстро всё происходит
        assertThat(sortMenu).hasValue("name,desc");
        actualNames = page.getByTestId("product-name").allInnerTexts();
        expectedNames = new ArrayList<>(actualNames);
        Collections.sort(expectedNames, Collections.reverseOrder());
        //List<String> expectedNames = actualNames.stream().sorted(Comparator.reverseOrder())
        Assertions.assertThat(actualNames).isEqualTo(expectedNames);
        Assertions.assertThat(actualNames).isSortedAccordingTo(Comparator.reverseOrder());
    }
}
