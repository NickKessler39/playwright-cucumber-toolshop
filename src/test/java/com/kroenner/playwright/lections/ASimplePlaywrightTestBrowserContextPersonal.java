package com.kroenner.playwright.lections;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;


public class ASimplePlaywrightTestBrowserContextPersonal {
    //Обычная переменная (без static): Принадлежит конкретному «экземпляру» (объекту).
    //Это как номер телефона в твоем личном мобильнике. У каждого студента в классе свой телефон.
    //Статичная переменная (static): Принадлежит всему классу.
    //Это как настенные часы в школьном кабинете. Они одни на всех. Неважно, сколько студентов зашло в класс, часы — общие.
    //Зачем это в JUnit? JUnit работает так: для каждого нового теста (@Test) он создает новый экземпляр твоего класса ASimplePlaywrightTestBrowserContext.
    //Если переменные playwright и browser будут обычными (не статичными), то при запуске 10 тестов JUnit попытается 10 раз запустить браузер с нуля.
    //Когда мы пишем static, мы говорим: «Эти объекты общие для всех тестов в этом классе».
    //Именно поэтому метод @BeforeAll обязан быть статичным — он запускается один раз, когда самого объекта теста еще даже не существует, есть только «кабинет» (класс).
    private static Playwright playwright; //static чтобы делиться этими переменными в тесте, private чтобы не делиться с другими классами
    private static Browser browser;
    private static BrowserContext browserContext;
    Page page;

    @BeforeAll //BeforeEach меняем на BeforeAll, чтоб перед всеми тестами создавать 1 браузерный инстанс
    public static void setUpBrowser() { //он должен был статичный
        playwright = Playwright.create();  //create environment, инициируем объект Playwright (запускаем движок)
        browser = playwright.chromium().launch( //open browser, инициируем объект Browser (просим у движка запустить процесс браузера, поэтому ".playwright.chromium()")
                new BrowserType.LaunchOptions() //добавляем LaunchOptions в playwright.chromium().launch()
                        .setHeadless(false)
                        .setArgs(Arrays.asList("--disable-extensions", "--disable-notifications"))
        );
        //убрали отсюда browserContext = browser.newContext(), потому что хотим отдельный контекст для каждого из тестов
    }

    @BeforeEach
    public void setUp() {
        browserContext = browser.newContext(); //теперь контекст у нас тут создается для каждого теста
        page = browserContext.newPage(); //страница создается от одного контекста из BeforeAll

    }

    @AfterEach
    public void tearDown() { //teardown из static снова стал public, так как он для каждого теста отдельно, а не для всех сразу (как при static)
        browserContext.close(); //после КАЖДОГО теста удаляем контекст
    }

    @AfterAll
    public static void tearDownFinal() { //а это уже статичный teardown, в нем мы сносим "гостиницу", то есть закрываем браузер и останавливаем движок
        browser.close(); //close browser
        playwright.close(); //shutdown browser
    }
//Итог:
// В «стартовом» варианте (с launch() в @BeforeEach): при каждом тесте процесс chromium.exe полностью исчезает и появляется заново. Это вызывает нагрузку на диск и процессор.
// В нашем новом варианте: процесс chromium.exe запускается один раз при старте @BeforeAll и висит в памяти неподвижно.
// Когда ты видишь, что «окошко закрылось и открылось», на самом деле закрывается и открывается только графическая оболочка (рендеринг),
//а «двигатель» под капотом продолжает работать.

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
