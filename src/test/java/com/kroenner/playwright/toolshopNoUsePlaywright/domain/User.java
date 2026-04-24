package com.kroenner.playwright.toolshopNoUsePlaywright.domain;
import net.datafaker.Faker;

public record User(String first_name,
                   String last_name,
                   Address address,
                   String phone,
                   String dob,
                   String email,
                   String password) {

    public static User randomUser() {
        Faker fake = new Faker();

        Address randomAddress = new Address(
                fake.address().streetAddress(),
                fake.address().city(),
                fake.address().state(),
                fake.address().country(),
                fake.address().postcode()
        );

        return new User(
                fake.name().firstName(),
                fake.name().lastName(),
                randomAddress,
                fake.phoneNumber().cellPhone(),
                fake.timeAndDate().birthday("yyyy-MM-dd"),
                fake.internet().emailAddress(),
                "SuperSecure@123"
        );
    }
    public User withPassword(String custom_password) {
        return new User(
                first_name,
                last_name,
                address,
                phone,
                dob,
                email,
                custom_password);
    }
}
