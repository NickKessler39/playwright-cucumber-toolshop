package com.kroenner.playwright.toolshopNoUsePlaywright.cucumber.stepdefinitions;

import com.microsoft.playwright.Tracing;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScenarioTracingFixtures {

    @Before
    public void setupTrace() {
        PlaywrightCucumberFixtures.getBrowserContext().tracing().start(
                new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true)
        );
    }

    @After
    public void recordTrace(Scenario scenario) { //чтоб создавать trace.zip для каждого тестового метода отдельно
        // 1. Очищаем имя от кавычек и прочих запрещенных символов для файловой системы
        String safeName = scenario.getName()
                .replace(" ", "-")
                .replaceAll("[^a-zA-Z0-9\\.\\-]", "")
                .toLowerCase();

        Path tracePath = Paths.get("target/tracer/trace-" + safeName + ".zip");

        // 2. Останавливаем трейс и записываем файл на диск
        PlaywrightCucumberFixtures.getBrowserContext().tracing().stop(
                new Tracing.StopOptions().setPath(tracePath)
        );

        // 3. ПРИКРЕПЛЯЕМ К ALLURE
        // Мы делаем это через проверку существования файла, чтобы не поймать ошибку
        if (Files.exists(tracePath)) {
            try (InputStream is = Files.newInputStream(tracePath)) {
                // В самом Allure можно оставить оригинальное имя сценария (там кавычки не страшны)
                Allure.addAttachment("Playwright Trace: " + scenario.getName(),
                        "application/zip", is, ".zip");
            } catch (IOException e) {
                System.err.println("Не удалось прикрепить трейс к Allure: " + e.getMessage());
            }
        }
    }
}
