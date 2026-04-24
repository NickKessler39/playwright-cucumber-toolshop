package com.kroenner.playwright.toolshopNoUsePlaywright.cucumber.stepdefinitions;

import com.kroenner.playwright.toolshopNoUsePlaywright.catalog.pageobjects.NavBar;
import com.kroenner.playwright.toolshopNoUsePlaywright.catalog.pageobjects.ProductList;
import com.kroenner.playwright.toolshopNoUsePlaywright.catalog.pageobjects.SearchComponent;
import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.ProductSummary;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.DataTableType;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.Map;


public class ProductCatalogStepDefinition {

    NavBar navBar;
    SearchComponent searchComponent;
    ProductList productList;

    @Before
    public void setUpPageObjects() {
        navBar = new NavBar(PlaywrightCucumberFixtures.getPage());
        searchComponent = new SearchComponent(PlaywrightCucumberFixtures.getPage());
        productList = new ProductList(PlaywrightCucumberFixtures.getPage());
    }

    @Given("Sally is on the home page")
    public void sally_is_on_the_home_page() {
        navBar.openHomePage();
    }

    @When("she searches for {string}")
    public void she_searches_for(String searchTerm) {
        searchComponent.searchBy(searchTerm);
    }

    @Then("the {string} product should be displayed")
    public void the_product_should_be_displayed(String productName) {
        var matchingProducts = productList.getProductNames();
        Assertions.assertThat(matchingProducts).contains(productName);
    }

    @Then("the following products should be displayed:")
    public void theFollowingProductsShouldBeDisplayed(List<String> expectedProducts) {
        var matchingProducts = productList.getProductNames();
        Assertions.assertThat(matchingProducts).containsAll(expectedProducts);
    }

    @DataTableType //Это «инструкция по сборке».
    //Ты говоришь Кукумберу: «Если в сценарии ты встретишь таблицу, а в методе тебе понадобится список ProductSummary, используй вот этот рецепт».
    public ProductSummary productSummaryRow(Map<String, String> productData) { //На вход: Cucumber берет одну строку из таблицы в виде Map<String, String>
        //(где ключи — это заголовки Product и Price).
        return new ProductSummary(
                productData.get("Product"),
                productData.get("Price"));
        //На выход: Он вызывает твой метод productSummaryRow и получает готовый объект.
    }

    @Then("the following products and their prices should be displayed:")
    //Cucumber смотрит на аргумент твоего метода: List<ProductSummary> expectedProductSummaries.
    //Он понимает: «Так, мне нужен список объектов ProductSummary. У меня как раз есть @DataTableType для этого типа!».
    //Он прогоняет каждую строчку таблицы через твой конвертер и собирает их в аккуратный список.
    public void theFollowingProductsAndTheirPricesShouldBeDisplayed(List<ProductSummary> expectedProductSummaries) {
        List<ProductSummary> matchingProducts = productList.getProductSummaries();
//        var matchingProducts = productList.getProductSummaries();
//        List<Map<String, String>> expectedProductData = expectedProducts.asMaps();
//        List<ProductSummary> expectedProductSummaries =
//                expectedProductData.stream().map(productData -> new ProductSummary(
//                        productData.get("Product"),
//                        productData.get("Price")
//                )).toList();
//
        Assertions.assertThat(matchingProducts).containsExactlyInAnyOrderElementsOf(expectedProductSummaries);
    }

    @Then("no products should be displayed")
    public void noProductsShouldBeDisplayed() {
        List<ProductSummary> matchingProducts = productList.getProductSummaries();
        Assertions.assertThat(matchingProducts).isEmpty();
    }

    @And("the message {string} should be displayed")
    public void theMessageShouldBeDisplayed(String messageText) {
        String completionMessage = productList.getSearchCompletedMessage();
        Assertions.assertThat(completionMessage).matches(messageText);
    }

    @And("she filters by {string}")
    public void sheFiltersBy(String filterName) {
        searchComponent.filterBy(filterName);
    }

    @When("she sorts by {string}")
    public void sheSortsBy(String sortFilter) {
        searchComponent.sortBy(sortFilter);
    }

    @Then("the first product  displayed should be {string}")
    public void theFirstProductDisplayedShouldBe(String productName) {
        var matchingProducts = productList.getProductNames().getFirst();
        Assertions.assertThat(matchingProducts).isEqualTo(productName);
        //Assertions.assertThat(matchingProducts).startsWith(productName);
    }
}
