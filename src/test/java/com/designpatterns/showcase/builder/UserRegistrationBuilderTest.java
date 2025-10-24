package com.designpatterns.showcase.builder;

import com.designpatterns.showcase.builder.lombok.UserRegistration;
import com.designpatterns.showcase.builder.lombok.UserRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserRegistrationBuilderTest {

    private UserRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new UserRegistrationService();
    }

    @Test
    void shouldBuildBasicRegistration() {
        UserRegistration registration = UserRegistration.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .termsAccepted(true)
                .build();

        assertNotNull(registration);
        assertEquals("johndoe", registration.getUsername());
        assertEquals("john@example.com", registration.getEmail());
        assertEquals("password123", registration.getPassword());
        assertEquals("John", registration.getFirstName());
        assertEquals("Doe", registration.getLastName());
        assertTrue(registration.isTermsAccepted());
        assertFalse(registration.isMarketingOptIn());
        assertFalse(registration.isEmailVerified());
    }

    @Test
    void shouldBuildRegistrationWithOptionalFields() {
        UserRegistration registration = UserRegistration.builder()
                .username("janedoe")
                .email("jane@example.com")
                .password("securepass123")
                .firstName("Jane")
                .lastName("Doe")
                .phoneNumber("555-1234")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62701")
                .country("USA")
                .termsAccepted(true)
                .marketingOptIn(true)
                .build();

        assertTrue(registration.getPhoneNumber().isPresent());
        assertEquals("555-1234", registration.getPhoneNumber().get());
        assertTrue(registration.getDateOfBirth().isPresent());
        assertEquals(LocalDate.of(1990, 1, 1), registration.getDateOfBirth().get());
        assertTrue(registration.getAddress().isPresent());
        assertTrue(registration.isMarketingOptIn());
    }

    @Test
    void shouldBuildCorporateRegistration() {
        UserRegistration registration = UserRegistration.builder()
                .username("bobsmith")
                .email("bob@company.com")
                .password("password123")
                .firstName("Bob")
                .lastName("Smith")
                .companyName("Acme Corp")
                .jobTitle("Software Engineer")
                .referralCode("REF123")
                .termsAccepted(true)
                .build();

        assertTrue(registration.getCompanyName().isPresent());
        assertEquals("Acme Corp", registration.getCompanyName().get());
        assertTrue(registration.getJobTitle().isPresent());
        assertEquals("Software Engineer", registration.getJobTitle().get());
        assertTrue(registration.getReferralCode().isPresent());
        assertEquals("REF123", registration.getReferralCode().get());
    }

    @Test
    void shouldValidateSuccessfully() {
        UserRegistration registration = UserRegistration.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .termsAccepted(true)
                .build();

        assertDoesNotThrow(registration::validate);
    }

    @Test
    void shouldThrowExceptionWhenUsernameMissing() {
        UserRegistration registration = UserRegistration.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .termsAccepted(true)
                .build();

        Exception exception = assertThrows(IllegalStateException.class, registration::validate);
        assertEquals("Username is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailMissing() {
        UserRegistration registration = UserRegistration.builder()
                .username("testuser")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .termsAccepted(true)
                .build();

        Exception exception = assertThrows(IllegalStateException.class, registration::validate);
        assertEquals("Email is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailInvalid() {
        UserRegistration registration = UserRegistration.builder()
                .username("testuser")
                .email("invalid-email")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .termsAccepted(true)
                .build();

        Exception exception = assertThrows(IllegalStateException.class, registration::validate);
        assertEquals("Email is invalid", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordTooShort() {
        UserRegistration registration = UserRegistration.builder()
                .username("testuser")
                .email("test@example.com")
                .password("short")
                .firstName("Test")
                .lastName("User")
                .termsAccepted(true)
                .build();

        Exception exception = assertThrows(IllegalStateException.class, registration::validate);
        assertEquals("Password must be at least 8 characters", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFirstNameMissing() {
        UserRegistration registration = UserRegistration.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .lastName("User")
                .termsAccepted(true)
                .build();

        Exception exception = assertThrows(IllegalStateException.class, registration::validate);
        assertEquals("First name is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLastNameMissing() {
        UserRegistration registration = UserRegistration.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .termsAccepted(true)
                .build();

        Exception exception = assertThrows(IllegalStateException.class, registration::validate);
        assertEquals("Last name is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTermsNotAccepted() {
        UserRegistration registration = UserRegistration.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        Exception exception = assertThrows(IllegalStateException.class, registration::validate);
        assertEquals("Terms must be accepted", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserTooYoung() {
        UserRegistration registration = UserRegistration.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .dateOfBirth(LocalDate.now().minusYears(10))
                .termsAccepted(true)
                .build();

        Exception exception = assertThrows(IllegalStateException.class, registration::validate);
        assertEquals("User must be at least 13 years old", exception.getMessage());
    }

    @Test
    void shouldUseServiceForBasicRegistration() {
        UserRegistration registration = service.createBasicRegistration(
                "johndoe",
                "john@example.com",
                "password123",
                "John",
                "Doe"
        );

        assertNotNull(registration);
        assertEquals("johndoe", registration.getUsername());
        assertTrue(registration.isTermsAccepted());
    }

    @Test
    void shouldUseServiceForFullRegistration() {
        UserRegistration registration = service.createFullRegistration(
                "janedoe",
                "jane@example.com",
                "password123",
                "Jane",
                "Doe",
                "555-1234",
                "123 Main St",
                "Springfield",
                "IL",
                "62701",
                "USA",
                true
        );

        assertNotNull(registration);
        assertTrue(registration.getPhoneNumber().isPresent());
        assertTrue(registration.isMarketingOptIn());
    }

    @Test
    void shouldUseServiceForCorporateRegistration() {
        UserRegistration registration = service.createCorporateRegistration(
                "bobsmith",
                "bob@company.com",
                "password123",
                "Bob",
                "Smith",
                "Acme Corp",
                "Engineer",
                "REF123"
        );

        assertNotNull(registration);
        assertTrue(registration.getCompanyName().isPresent());
        assertEquals("Acme Corp", registration.getCompanyName().get());
    }

    @Test
    void shouldReturnEmptyOptionalForMissingFields() {
        UserRegistration registration = UserRegistration.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .termsAccepted(true)
                .build();

        assertFalse(registration.getPhoneNumber().isPresent());
        assertFalse(registration.getDateOfBirth().isPresent());
        assertFalse(registration.getAddress().isPresent());
        assertFalse(registration.getReferralCode().isPresent());
    }
}
