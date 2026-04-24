package com.kroenner.playwright.toolshopNoUsePlaywright.fixtures;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Tracing;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface WithTracing {
    //Этот метод — "мост". Мы реализуем его в другом месте
    BrowserContext getBrowserContext();

    @BeforeEach
    default void setupTrace() {
        getBrowserContext().tracing().start(
                new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true)
        );
    }

    @AfterEach
    default void recordTrace(TestInfo testInfo) throws IOException {
        String methodName = testInfo.getTestMethod()
                .map(java.lang.reflect.Method::getName)
                .orElse("unknown-test");

        Path tracePath = Paths.get("target/tracer/trace-" + methodName + ".zip");

        // 1. Останавливаем запись и сохраняем файл
        getBrowserContext().tracing().stop(new Tracing.StopOptions().setPath(tracePath));

        // 2. Читаем файл и отправляем его в Allure
        if (Files.exists(tracePath)) {
            try (var is = Files.newInputStream(tracePath)) {
                Allure.addAttachment("Playwright Trace - " + methodName, "application/zip", is, ".zip");
            }
        }
    }
}
