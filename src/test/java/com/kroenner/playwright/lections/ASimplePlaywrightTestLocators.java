package com.kroenner.playwright.lections;

import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.*;

@UsePlaywright(ASimplePlaywrightTestLocators.MyOptions.class) //просим аннотацию заглянуть в класс MyOptions
// В этой библиотеке (playwright-junit) объект Page создается «магическим» образом и передается прямо в аргументы тестового метода.
public class ASimplePlaywrightTestLocators {
    // 1. Убираем (или не используем) private Page page; вверху — она нам не нужна
    public static class MyOptions implements OptionsFactory { //обещаем Playwright, что создадим метод getOptions (именно такой!), который вернет все настройки
        //когда пишешь implements OptionsFactory, ты можешь нажать Alt + Enter, и среда сама создаст заготовку метода с правильным названием, чтобы ты не ошибся ни в одной букве

        @Override
        public Options getOptions() { //имлементируем обещанный метод с кастомными настройками
            return new Options()
                    .setHeadless(Boolean.parseBoolean(System.getProperty("headless", "true")))
                    .setTestIdAttribute("data-test") //на сайте вместо стандартного тега id используется свой - "data-test", мы объясняем Playwright, что это тоже id
                    //ее также можно добавить прямо в тестовый метод, но из-за аннотации @UsePlaywright, нужно будет передать Playwright playwright в скобки названия метода
                    //а также прописать в самом методе playwright.selectors().setTestIdAttribute("data-test");
                    .setLaunchOptions(
                            new BrowserType.LaunchOptions()
                                    .setArgs(Arrays.asList("--disable-extensions", "--disable-notifications"))
                                    //.setSlowMo(500)
                    );
        }
    }

    @BeforeEach
    void openTheCataloguePage(Page page) { //Открываем сайт перед каждым тестом, // Просим Playwright дать нам страницу сюда из @UsePlaywright
        openPage(page); // Передаем её в метод помощник
    }

    void openPage(Page page) { // Принимаем страницу
        page.navigate("https://practicesoftwaretesting.com/");
    }

    @DisplayName("Smoke test for locators")
    @Test
    void trySomeLocators(Page page) { // Playwright даст ту же самую страницу сюда
        page.locator("[placeholder=Search]").fill("Pliers");
        page.locator("[type=Submit]").click();
        //page.pause(); //это откроет Playwright Inspector
        assertThat(page.getByText("Combination Pliers")).isVisible(); //ищем любой элемент с этим текстом
        assertThat(page.getByAltText("Combination Pliers")).isVisible(); //ищем элемент с картинкой, у которой есть alt аттрибут с текстом
        assertThat(page.getByTitle("Practice Software Testing - Toolshop")).isVisible(); //как правило title это маленький текст, который показывается при наведении мыши
    }

    @DisplayName("Using text")
    @Test
    void byText(Page page) {
        page.getByText("Bolt Cutters").click();

        PlaywrightAssertions.assertThat(page.getByText("MightyCraft Hardware")).isVisible();
        }

    @DisplayName("Using image text")
    @Test
    void byAltText(Page page) {
        page.getByAltText("Combination Pliers").click();

        PlaywrightAssertions.assertThat(page.getByText("ForgeFlex Tools")).isVisible();
    }

    @DisplayName("Using title")
    @Test
    void byTitle(Page page) {
        page.getByAltText("Combination Pliers").click();
        page.getByTitle("Practice Software Testing - Toolshop").click();
    }

    @DisplayName("Using label")
    @Test
    void byLabel(Page page) {
        page.getByText("Sign in").click();
        page.getByLabel("Email address").fill("obi-wan@kenobi.com");
        page.getByTitle("Practice Software Testing - Toolshop").click();
        page.getByLabel("Hammer").check(); //Checkbox тоже по лейблу ищется
    }

    @DisplayName("Using placeholder")
    @Test
    void byPlaceholder(Page page) {
        page.locator("[href='/auth/login']").click(); //альтернатива по аттрибуту
        page.getByPlaceholder("Your email").fill("obi-wan@kenobi.com");
    }

    @DisplayName("Using test id")
    @Test
    void byTestId(Page page) {
        page.getByTestId("search-query").fill("Claw");
        page.getByTestId("search-submit").click();
        assertThat(page.getByTestId("search_completed")).isVisible(); //существует ли результат поиска с таким id?
        //Внутри элемента с id (data-test) search_completed найди все id, начинающиеся с 'product-', но найти только прямых потомков, а не "внуков" и "правнуков" (значок >)
        assertThat(page.locator("[data-test='search_completed'] > [data-test^='product-']")).hasCount(3);
        assertThat(page.locator("[data-test='search_completed'] > [data-test^='product-']").first())
                .containsText("Claw"); //а есть ли в названии первого элемента слово "Claw"?
    }

    @DisplayName("Using Roles")
    @Test
    void byRole(Page page) {
        page.getByRole(AriaRole.MENUBAR, new Page.GetByRoleOptions().setName("Main menu")). //Пример Nested Elements, ищем менюбар с именем "Main menu"
                getByRole(AriaRole.MENUITEM, new Locator.GetByRoleOptions().setName("Home")).click(); //который содержит менюайтем с именем "Home"

        page.getByRole(AriaRole.MENUITEM, new Page.GetByRoleOptions().setName("Main menu")).
                getByText("Home").click(); //пример,  что можно миксовать стратегии

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Contact")).click(); //Ищем и кликаем линк с текстом Contact
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Contact"))).hasCount(1); //Проверяем, что на открывшейся странице
                                                                                                                  //Только один элемент с роль "Заголовок" и текстом Contact
        page.getByTitle("Practice Software Testing - Toolshop").click(); //Возвращаемся на главную страницу
        page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Hammer")).check(); //Отмечаем чекбокс с текстом "Hammer"
        assertThat(page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setChecked(true))).hasCount(1); //Сколько отмеченных чекбоксов? Должен быть 1
        page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Wrench")).check(); //Отмечаем чекбокс с текстом "Wrench"
        assertThat(page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setChecked(true))).hasCount(2); //Сколько отмеченных чекбоксов? Должно быть 2
        page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Wrench")).uncheck(); //Убираем чекбокс с текстом "Wrench"
        assertThat(page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setChecked(true))).hasCount(1); //Сколько отмеченных чекбоксов? Снова должен быть 1

        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Search")).fill("Hammer"); //заполняем поле Search значением Hammer
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click(); //Жмем кнопку "Search"
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Hammer").setLevel(5))).hasCount(7); //Сколько элементов Header уровня 5 нашлось? (h5)
                                                                                                                             //Должно быть 7

    }

    @DisplayName("Using collections")
    @Test
    void workingWithCollections(Page page) {
        int itemsOnThePage = page.locator(".card").count(); //считаем сколько на странице есть элементов с классом card
        page.locator(".card").first().locator("h5").innerText(); //что за текст есть первом в элементе уровня h5
        page.locator(".card").last().locator("h5").innerText(); //что за текст есть последнем в элементе уровня h5
        page.locator(".card").nth(2).locator("h5").innerText(); //что за текст есть во третьем в элементе уровня h5 (счет от 0)
        List<String> itemNames = page.getByTestId("product-name").allTextContents(); //вытянет текст каждого элемента и превратит это в список
        List<String> filteredNames = page.getByTestId("product-name")
                .filter(new Locator.FilterOptions().setHasText("Cutters"))
                .allTextContents();
        List<String> filteredNames2 = page.locator(".card") //берем весь "блок" элементов товара
                .filter(new Locator.FilterOptions().setHas(page.getByText("Out of stock"))) //фильтруем по тексту
                .getByTestId("product-name") //ищем все элементы с этим айди, попадающие под предыдущие критерии
                        .allTextContents();
    }

    @DisplayName("Using CSS locators")
    @Test
    void workingWithCSS(Page page) {
        assertThat(page.locator("#navbarSupportedContent")).isVisible(); //css id selector
        //xpath = //div[@class='menu-item']
        assertThat(page.locator(".nav-link.active:has-text('Home')")).isVisible(); //элемент с классом nav-link active (пробел заменяем точкой), который содержит текст
        //xpath = //a[contains(@class, 'nav-link') and text()='Home']
        assertThat(page.locator("input[id='search-query']")).isVisible(); //элемент типа input с id search-query
        //xpath = //input[@id='search-query']
        //.btnSubmit - выбираем элементы с классом btnSubmit
        assertThat(page.locator("[placeholder=Search]")).isVisible();
    }

    @DisplayName("Using CSS locators in Contact")
    @Test
    void workingWithCSSContact(Page page) {
        page.navigate("https://practicesoftwaretesting.com/contact");
        page.locator("#first_name").fill("Sarah-Jane");
        PlaywrightAssertions.assertThat(page.locator("#first_name")).hasValue("Sarah-Jane");

        page.locator(".btnSubmit").click();
        assertThat(page.locator(".alert:has-text('Last name is required')")).isVisible(); //проверяем конкретную ошибку класса alert с текстом
        /* Здесь мы работаем со стрингой, а assertThat от Playwright не принимает никакие значения, кроме page и locator, поэтому пришлось импортировать библиотеку AssertJ
        List<String> alertMessages = page.locator(".alert").allTextContents(); //собираем содержимое всех элементов класса alert
        Assertions.assertTrue(!alertMessages.isEmpty()); //проверяем, что содержимое стринги alertMessages не пустое, тут используется AssertJ
        */
        page.getByRole(AriaRole.ALERT).first().isVisible(); //тут стараемся не прибегать к AssertJ
        assertThat(page.getByRole(AriaRole.ALERT)).not().hasCount(0); //тут стараемся не прибегать к AssertJ, но также проверяем, что хоть один алерт есть

        page.locator("[placeholder='Your last name *']").fill("Smith"); //полное совпадение по тексту "Your last name *"'"
        page.locator("[placeholder*='Your last name']").fill("Smith"); //placeholder*= ищет элемент с указанным содержимым в любом месте строки
        page.locator("[placeholder^='Your last']").fill("Smith"); //placeholder^= ищет элемент, начинающийся c "Your last"
        page.locator("[placeholder$='last name *']").fill("Smith"); //placeholder$= ищет элемент, заканчивающийся на "last name *"
        page.locator("input[placeholder='Your last name *']").fill("Smith"); //уточняем, что это input field
        assertThat(page.locator("#last_name")).hasValue("Smith");
    }

    @DisplayName("Interacting with text fields")
    @Test
    void fieldValues(Page page) {
        var firstNameField = page.getByLabel("First name"); //var это тоже самое, что и Locator firstNameField = page.getByLabel("First name");
        var lastNameField = page.getByLabel("Last name");
        var emailField = page.getByLabel("Email address");
        var messageField = page.getByLabel("Message");
        var subjectField = page.getByLabel("Subject");

        page.navigate("https://practicesoftwaretesting.com/contact");

        firstNameField.fill("Sarah-Jane");
        lastNameField.fill("Kessler");
        emailField.fill("sjkessler@mail.com");
        messageField.fill("Hello, world!\nВторая строка\nТретья строка");
        subjectField.selectOption("Warranty");

        assertThat(firstNameField).hasValue("Sarah-Jane");
        assertThat(lastNameField).not().hasValue("Kek");
        assertThat(lastNameField).hasValue("Kessler");
        assertThat(emailField).hasValue("sjkessler@mail.com");
        assertThat(messageField).hasValue("Hello, world!\nВторая строка\nТретья строка");
        assertThat(subjectField).hasValue("warranty"); //в коде там маленькая буква
        assertThat(subjectField.locator("option:checked")).hasText("Warranty");
        //в HTML у выпадающих списков (<select>) есть стандартное поведение:
        //когда ты выбираешь какой-то пункт, браузер вешает на него невидимый флаг «выбран».
        //В CSS этот флаг называется псевдоклассом :checked.
        //subjectField — это наш <select>.
        //.locator("option") — говорит Playwright: «Посмотри внутрь этого селекта и найди там все теги <option> (строчки списка)».
        //:checked — это фильтр. Он говорит: «Из всех строчек оставь только ту, которую пользователь выбрал в данный момент».

        subjectField.selectOption(new SelectOption().setIndex(2));
        assertThat(subjectField).hasValue("webmaster");

        int initialHeight = (int) messageField.boundingBox().height;
        messageField.fill("1\n2\n3\n4\n5\n6\n7\n8\n9\n10");
        int finalHeight = (int) messageField.boundingBox().height;
        org.assertj.core.api.Assertions.assertThat(finalHeight).isEqualTo(initialHeight);
        Assertions.assertTrue(finalHeight == initialHeight);
    }

    @DisplayName("Uploading files")
    @Test
    void uploadingFiles(Page page) throws Exception {
        page.navigate("https://practicesoftwaretesting.com/contact");
        var uploadFile = page.getByLabel("Attachment"); //локатор поля загрузки (там где имя файла пишется)
        var sendButton = page.getByTestId("contact-submit");
        //Тут из файла делаем объект Path, чтоб его можно было передать дальше
        Path correctFile = Paths.get(ClassLoader.getSystemResource("UploadTestEmpty.txt").toURI()); //getSystemResource сразу в resources ищет
        Path incorrectFile = Paths.get(ClassLoader.getSystemResource("UploadTest.txt").toURI());
        Path incorrectFileType = Paths.get(ClassLoader.getSystemResource("UploadTestHtml.html").toURI());

        page.setInputFiles("#attachment", correctFile); //тут загружаем файл через "Обзор"
        String uploadedFile = uploadFile.inputValue(); //считаем из поля загрузки имя загруженного файла
        org.assertj.core.api.Assertions.assertThat(uploadedFile).endsWith("TestEmpty.txt"); //проверяем имя
        sendButton.click();
        assertThat(page.getByTestId("attachment-error")).not().isVisible();

        page.setInputFiles("#attachment", incorrectFile);
        org.assertj.core.api.Assertions.assertThat(uploadFile.inputValue()).endsWith("Test.txt");
        sendButton.click();
        assertThat(page.getByTestId("attachment-error")).hasText("File should be empty.");

        page.setInputFiles("#attachment", incorrectFileType);
        org.assertj.core.api.Assertions.assertThat(uploadFile.inputValue()).endsWith("TestHtml.html");
        sendButton.click();
        assertThat(page.getByTestId("attachment-error")).hasText("File should have a txt extension.");
    }

    @DisplayName("Mandatory Fields test")
    @ParameterizedTest //тест будет пробегать 1 раз с каждым параметром из ValueSouce
    @ValueSource(strings = {"First name", "Last name", "Email", "Message"}) //Определили, что параметры у нас типа String и их 4, значит тест пробежит 4 раза
    void mandatoryFields(String fieldName, Page page) { //Передаем параметры типа String в метод, присваивая их значение fieldName
        /*ВАЖНО, порядок параметров в методе (тесте):
        Когда ты используешь @ParameterizedTest, JUnit берет на себя управление параметрами метода (теста).
        Первый помощник (от Playwright) хочет вставить в метод объект Page.
        Второй помощник (от JUnit Params) видит @ValueSource и пытается сопоставить параметры по порядку.
        Проблема в том, что @ParameterizedTest ожидает, что параметры из источника (ValueSource) будут идти первыми.
        Если JUnit видит Page page на первом месте, смотрит в свой список строк {"First name", ...} и не понимает, как засунуть "First name" в объект Page.
        Происходит «кораблекрушение».
        */


        var firstNameField = page.getByLabel("First name");
        var lastNameField = page.getByLabel("Last name");
        var emailField = page.getByLabel("Email address");
        var messageField = page.getByLabel("Message");
        var subjectField = page.getByLabel("Subject");
        var sendButton = page.getByText("Send");


        page.navigate("https://practicesoftwaretesting.com/contact");

        //Fill in the field values
        firstNameField.fill("Sarah-Jane");
        lastNameField.fill("Kessler");
        emailField.fill("sjkessler@mail.com");
        messageField.fill("Hello, world!");
        subjectField.selectOption("Warranty");

        //Clear one of the fields
        page.getByLabel(fieldName).clear();
        sendButton.click();

        //Check the error message for that field
        var errorMessage = page.getByRole(AriaRole.ALERT).getByText(fieldName  + " is required");
        assertThat(errorMessage).isVisible();
    }

}
