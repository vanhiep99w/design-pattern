package com.designpatterns.showcase.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificationFactoryTest {

    private NotificationFactory.NotificationServiceProvider provider;
    private EmailNotificationService emailService;
    private SmsNotificationService smsService;
    private PushNotificationService pushService;

    @BeforeEach
    void setUp() {
        emailService = new EmailNotificationService();
        smsService = new SmsNotificationService();
        pushService = new PushNotificationService();
        
        provider = new NotificationFactory.NotificationServiceProvider(
                Arrays.asList(emailService, smsService, pushService)
        );
    }

    @Test
    void shouldReturnEmailService() {
        NotificationService service = provider.getNotificationService("EMAIL");
        
        assertNotNull(service);
        assertEquals("EMAIL", service.getNotificationType());
        assertInstanceOf(EmailNotificationService.class, service);
    }

    @Test
    void shouldReturnSmsService() {
        NotificationService service = provider.getNotificationService("SMS");
        
        assertNotNull(service);
        assertEquals("SMS", service.getNotificationType());
        assertInstanceOf(SmsNotificationService.class, service);
    }

    @Test
    void shouldReturnPushService() {
        NotificationService service = provider.getNotificationService("PUSH");
        
        assertNotNull(service);
        assertEquals("PUSH", service.getNotificationType());
        assertInstanceOf(PushNotificationService.class, service);
    }

    @Test
    void shouldHandleCaseInsensitiveNotificationType() {
        NotificationService service1 = provider.getNotificationService("email");
        NotificationService service2 = provider.getNotificationService("EmAiL");
        NotificationService service3 = provider.getNotificationService("EMAIL");
        
        assertEquals("EMAIL", service1.getNotificationType());
        assertEquals("EMAIL", service2.getNotificationType());
        assertEquals("EMAIL", service3.getNotificationType());
    }

    @Test
    void shouldHandleNotificationTypeWithWhitespace() {
        NotificationService service = provider.getNotificationService(" SMS ");
        
        assertNotNull(service);
        assertEquals("SMS", service.getNotificationType());
    }

    @Test
    void shouldThrowExceptionForUnsupportedNotificationType() {
        UnsupportedNotificationTypeException exception = assertThrows(
                UnsupportedNotificationTypeException.class,
                () -> provider.getNotificationService("FAX")
        );
        
        assertTrue(exception.getMessage().contains("Unsupported notification type"));
    }

    @Test
    void shouldThrowExceptionForNullNotificationType() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> provider.getNotificationService(null)
        );
        
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void shouldReturnTrueForSupportedNotificationType() {
        assertTrue(provider.isNotificationTypeSupported("EMAIL"));
        assertTrue(provider.isNotificationTypeSupported("SMS"));
        assertTrue(provider.isNotificationTypeSupported("PUSH"));
    }

    @Test
    void shouldReturnFalseForUnsupportedNotificationType() {
        assertFalse(provider.isNotificationTypeSupported("FAX"));
        assertFalse(provider.isNotificationTypeSupported("TELEGRAM"));
    }

    @Test
    void shouldReturnFalseForNullNotificationType() {
        assertFalse(provider.isNotificationTypeSupported(null));
    }

    @Test
    void shouldReturnAllServices() {
        Map<String, NotificationService> services = provider.getAllServices();
        
        assertEquals(3, services.size());
        assertTrue(services.containsKey("EMAIL"));
        assertTrue(services.containsKey("SMS"));
        assertTrue(services.containsKey("PUSH"));
    }

    @Test
    void shouldReturnImmutableCopyOfServices() {
        Map<String, NotificationService> services = provider.getAllServices();
        services.clear();
        
        Map<String, NotificationService> servicesAgain = provider.getAllServices();
        assertEquals(3, servicesAgain.size());
    }
}
