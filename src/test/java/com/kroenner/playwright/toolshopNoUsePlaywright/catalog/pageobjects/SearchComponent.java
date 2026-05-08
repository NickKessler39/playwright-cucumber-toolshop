package com.kroenner.playwright.toolshopNoUsePlaywright.catalog.pageobjects;

import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.ScreenshotManager;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

public class SearchComponent {
    private final Page page;

    public SearchComponent(Page page) {
        this.page = page;
    }

    @Step("Search by {keyword}")
    public void searchBy(String keyword) {

        //      page.waitForResponse("**/products/search?q=" + keyword, () -> {
        //          page.getByPlaceholder("Search").fill(keyword);
        //           page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
        //       });

        //Изменили код, чтоб работал тест Cucumber. Иначе он вечность ждет ответ в waitForResponse("**/products/search?q=" + keyword ());
        /*
        Когда ты используешь лямбду response -> response.url().contains("search"), ты делаешь проверку гибкой.
        Теперь Playwright неважно, есть ли там лишние параметры, закодированы ли пробелы как %20 или добавлен ли в конец какой-то технический токен.

        Если в адресе есть слово search и сервер ответил 200 OK — условие выполнено.
        */
        page.waitForResponse(
                response -> response.url().contains("search") && response.status() == 200,
                () -> {
                    page.getByPlaceholder("Search").fill(keyword);
                    page.locator("button:has-text('Search')").click();
                }
        );
        ScreenshotManager.takeScreenshot(page, "Search results for " + keyword);
    }

    @Step("Clear search field")
    public void clearSearch() {
        page.waitForResponse("**/products**", () -> {
            page.getByTestId("search-reset").click();
        });
        ScreenshotManager.takeScreenshot(page, "Search results field is cleared");
    }

    public void filterBy(String filterName) {
        page.waitForResponse(
                response -> response.url().contains("by_category") && response.status() == 200,
                () -> {
                    page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName(filterName)).click();
                }
        );
    }

    public void sortBy(String sortFilter) {
        page.waitForResponse(
                response -> response.url().contains("sort=") && response.status() == 200,
                () -> {
                    //page.getByRole(AriaRole.COMBOBOX).selectOption(sortFilter); //Этот вариант хуже, если будет большего одного dropdown поля
                    page.getByTestId("sort").selectOption(sortFilter);
                }
        );
    }
}
