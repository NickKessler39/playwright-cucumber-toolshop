package com.kroenner.playwright.toolshopNoUsePlaywright.contact.pageobjects;

import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.ScreenshotManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class ContactForm {
    private final Page page;
    private Locator firstNameField;
    private Locator lastNameField;
    private Locator emailField;
    private Locator messageField;
    private Locator subjectField;
    private Locator sendButton;


    public ContactForm(Page page) {
        this.page = page;
        this.firstNameField = page.getByLabel("First name");
        this.lastNameField = page.getByLabel("Last name");
        this.emailField = page.getByLabel("Email address");
        this.messageField = page.getByLabel("Message");
        this.subjectField = page.getByLabel("Subject");
        this.sendButton = page.getByText("Send");
    }

    @Step("Set name to {firstName}")
    public void setFirstName(String firstName) {
        firstNameField.fill(firstName);
        ScreenshotManager.takeScreenshot(page, "Set name to " +firstName);
    }
    @Step("Set last name to {lastName}")
    public void setLastName(String lastName) {
        lastNameField.fill(lastName);
        ScreenshotManager.takeScreenshot(page, "Set last name to " +lastName);
    }
    @Step("Set email to {email}")
    public void setEmail(String email) {
        emailField.fill(email);
        ScreenshotManager.takeScreenshot(page, "Set email to " +email);
    }
    @Step("Set message to {message}")
    public void setMessage(String message) {
        messageField.fill(message);
        ScreenshotManager.takeScreenshot(page, "Set message to " +message);
    }
    @Step("Set subject to {subject}")
    public void setSubject(String subject) {
        subjectField.selectOption(subject);
        ScreenshotManager.takeScreenshot(page, "Set subject to " +subject);
    }
    @Step("Add an attachment")
    public void setAttachment(Path fileToUpload) {
        page.setInputFiles("#attachment", fileToUpload);
        ScreenshotManager.takeScreenshot(page, "File uploaded");
    }

    @Step("Submit form")
    public void submitForm() {
        sendButton.click();
        ScreenshotManager.takeScreenshot(page, "Form submitted");
    }

    public String getAlertMessage() {
        return page.getByRole(AriaRole.ALERT).textContent();
    }

    public Locator getFirstNameField() {
        return firstNameField;
    }
    public Locator getLastNameField() {
        return lastNameField;
    }
    public Locator getEmailField() {
        return emailField;
    }
    public Locator getSubjectField() {
        return subjectField;
    }
    public Locator getMessageField() {
        return messageField;
    }

    @Step("Cleat field {fieldName}")
    public void clearField(String fieldName) {
        page.getByLabel(fieldName).clear();
        assertThat(page.getByLabel(fieldName)).hasClass(Pattern.compile(".*invalid.*")); //Playwright слишком быстр, нужно чтоб он подождал, пока у поля будет класс "invalid"
        ScreenshotManager.takeScreenshot(page, "Field " +fieldName+ " cleared");
    }
}
