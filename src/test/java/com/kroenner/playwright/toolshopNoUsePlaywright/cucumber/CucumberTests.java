package com.kroenner.playwright.toolshopNoUsePlaywright.cucumber;

import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("/features")
@ConfigurationParameters({
        @ConfigurationParameter(key="cucumber.plugin",
        value="io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm," +
                "pretty," +
                "html:target/cucumber-reports/cucumber.html"),
        @ConfigurationParameter(key="cucumber.glue",
        value="com.kroenner.playwright.toolshopNoUsePlaywright.cucumber") //заставляем Cucumber смотреть файлы
        })

public class CucumberTests {
}
