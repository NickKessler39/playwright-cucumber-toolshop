package com.kroenner.playwright.toolshopUsePlaywright;

import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.ScreenshotManager;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.AfterEach;

public interface TakesFinalScreenshot {

    @AfterEach
    default void takeScreenshot(Page page) {
        ScreenshotManager.takeScreenshot(page, "Final screenshot");
    }
}
