package com.designpatterns.showcase.builder.lombok;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Optional;

@Getter
@Builder
@ToString
public class UserRegistration {

    private final String username;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;

    private final String phoneNumber;
    private final LocalDate dateOfBirth;
    private final String address;
    private final String city;
    private final String state;
    private final String zipCode;
    private final String country;

    @Builder.Default
    private final boolean termsAccepted = false;

    @Builder.Default
    private final boolean marketingOptIn = false;

    @Builder.Default
    private final boolean emailVerified = false;

    private final String referralCode;
    private final String companyName;
    private final String jobTitle;

    public Optional<String> getPhoneNumber() {
        return Optional.ofNullable(phoneNumber);
    }

    public Optional<LocalDate> getDateOfBirth() {
        return Optional.ofNullable(dateOfBirth);
    }

    public Optional<String> getAddress() {
        return Optional.ofNullable(address);
    }

    public Optional<String> getCity() {
        return Optional.ofNullable(city);
    }

    public Optional<String> getState() {
        return Optional.ofNullable(state);
    }

    public Optional<String> getZipCode() {
        return Optional.ofNullable(zipCode);
    }

    public Optional<String> getCountry() {
        return Optional.ofNullable(country);
    }

    public Optional<String> getReferralCode() {
        return Optional.ofNullable(referralCode);
    }

    public Optional<String> getCompanyName() {
        return Optional.ofNullable(companyName);
    }

    public Optional<String> getJobTitle() {
        return Optional.ofNullable(jobTitle);
    }

    public void validate() {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalStateException("Username is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalStateException("Email is required");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalStateException("Email is invalid");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalStateException("Password must be at least 8 characters");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalStateException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalStateException("Last name is required");
        }
        if (!termsAccepted) {
            throw new IllegalStateException("Terms must be accepted");
        }
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now().minusYears(13))) {
            throw new IllegalStateException("User must be at least 13 years old");
        }
    }
}
