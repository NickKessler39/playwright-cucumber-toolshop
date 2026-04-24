package com.kroenner.playwright.toolshopNoUsePlaywright.contact;

import com.kroenner.playwright.toolshopNoUsePlaywright.fixtures.PlaywrightAbstractTestCase;
import com.kroenner.playwright.toolshopNoUsePlaywright.contact.pageobjects.ContactForm;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@DisplayName("Contact Form Tests")
@Feature("Contact Form")
public class ContactFormTest extends PlaywrightAbstractTestCase {

    ContactForm contactForm;

    @BeforeEach
    void openTheContactPage() {
        contactForm = new ContactForm(page);
        openPage();
    }
    void openPage() {
        page.navigate("https://practicesoftwaretesting.com/contact");
    }


    @DisplayName("Fill out the Contact Form")
    @Story("Basic input fields")
    @Test
    void completeForm() throws URISyntaxException {
        Path fileToUpload = Paths.get(ClassLoader.getSystemResource("UploadTestEmpty.txt").toURI());
        contactForm.setFirstName("Sarah-Jane");
        contactForm.setLastName("Kessler");
        contactForm.setEmail("sjkessler@mail.com");
        contactForm.setMessage("Hello, world!\nВторая строка\nТретья строка\nЧетвертая строка\nПятая строка");
        contactForm.setSubject("warranty");
        contactForm.setAttachment(fileToUpload);

        assertThat(contactForm.getFirstNameField()).hasValue("Sarah-Jane");
        assertThat(contactForm.getLastNameField()).not().hasValue("Kek");
        assertThat(contactForm.getLastNameField()).hasValue("Kessler");
        assertThat(contactForm.getEmailField()).hasValue("sjkessler@mail.com");
        assertThat(contactForm.getMessageField()).hasValue("Hello, world!\nВторая строка\nТретья строка\nЧетвертая строка\nПятая строка");
        assertThat(contactForm.getSubjectField()).hasValue("warranty");

        contactForm.submitForm();
        Assertions.assertThat(contactForm.getAlertMessage()).contains("Thanks for your message! We will contact you shortly.");
    }

    @DisplayName("Checking which fields are mandatory")
    @Story("Mandatory fields")
    @ParameterizedTest
    @ValueSource(strings = {"First name", "Last name", "Email", "Message"})
    void mandatoryFieldsCheck(String fieldName) {
        contactForm.setFirstName("Sarah-Jane");
        contactForm.setLastName("Kessler");
        contactForm.setEmail("sjkessler@mail.com");
        contactForm.setMessage("Hello, world!\nВторая строка\nТретья строка\nЧетвертая строка\nПятая строка");
        contactForm.setSubject("warranty");

        contactForm.clearField(fieldName);
        contactForm.submitForm();

        var errorMessage = page.getByRole(AriaRole.ALERT).getByText(fieldName + " is required");
        assertThat(errorMessage).isVisible();
    }
}
