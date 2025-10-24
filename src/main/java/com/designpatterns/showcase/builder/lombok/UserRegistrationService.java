package com.designpatterns.showcase.builder.lombok;

public class UserRegistrationService {

    public UserRegistration createBasicRegistration(String username, String email, String password,
                                                     String firstName, String lastName) {
        UserRegistration registration = UserRegistration.builder()
                .username(username)
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .termsAccepted(true)
                .build();

        registration.validate();
        return registration;
    }

    public UserRegistration createFullRegistration(String username, String email, String password,
                                                    String firstName, String lastName,
                                                    String phoneNumber, String address,
                                                    String city, String state, String zipCode,
                                                    String country, boolean marketingOptIn) {
        UserRegistration registration = UserRegistration.builder()
                .username(username)
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .address(address)
                .city(city)
                .state(state)
                .zipCode(zipCode)
                .country(country)
                .termsAccepted(true)
                .marketingOptIn(marketingOptIn)
                .build();

        registration.validate();
        return registration;
    }

    public UserRegistration createCorporateRegistration(String username, String email, String password,
                                                         String firstName, String lastName,
                                                         String companyName, String jobTitle,
                                                         String referralCode) {
        UserRegistration registration = UserRegistration.builder()
                .username(username)
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .companyName(companyName)
                .jobTitle(jobTitle)
                .referralCode(referralCode)
                .termsAccepted(true)
                .build();

        registration.validate();
        return registration;
    }
}
