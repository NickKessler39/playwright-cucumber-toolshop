package com.kroenner.playwright.toolshopNoUsePlaywright.domain;

public record Address(
        String street,
        String city,
        String state,
        String country,
        String postal_code
) {}

