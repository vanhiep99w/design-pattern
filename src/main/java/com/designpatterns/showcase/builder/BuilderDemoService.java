package com.designpatterns.showcase.builder;

import com.designpatterns.showcase.builder.domain.Invoice;
import com.designpatterns.showcase.builder.domain.InvoiceItem;
import com.designpatterns.showcase.builder.domain.OrderDetails;
import com.designpatterns.showcase.builder.lombok.UserRegistration;
import com.designpatterns.showcase.builder.manual.OrderBuilder;
import com.designpatterns.showcase.builder.query.Query;
import com.designpatterns.showcase.builder.query.QueryBuilder;
import com.designpatterns.showcase.common.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuilderDemoService {

    public Invoice createSampleInvoice() {
        List<InvoiceItem> items = new ArrayList<>();
        
        items.add(InvoiceItem.builder()
                .description("Professional Services - Software Development")
                .quantity(40)
                .unitPrice(new BigDecimal("150.00"))
                .build());
        
        items.add(InvoiceItem.builder()
                .description("Cloud Hosting - Monthly Fee")
                .quantity(1)
                .unitPrice(new BigDecimal("299.99"))
                .build());
        
        items.add(InvoiceItem.builder()
                .description("Support Package - Premium")
                .quantity(1)
                .unitPrice(new BigDecimal("500.00"))
                .build());

        return Invoice.builder()
                .invoiceNumber("INV-2024-001")
                .customerName("Tech Solutions Inc.")
                .customerEmail("billing@techsolutions.com")
                .customerAddress("456 Innovation Drive, San Francisco, CA 94105")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .items(items)
                .taxRate(new BigDecimal("0.0875"))
                .discountAmount(new BigDecimal("100.00"))
                .paymentTerms("Net 30")
                .notes("Thank you for your business!")
                .build();
    }

    public OrderDetails createSampleOrder(Long userId) {
        return OrderBuilder.newOrder()
                .forUser(userId)
                .addItem("MacBook Pro 16-inch", 1, new BigDecimal("2499.00"))
                .addItem("Magic Mouse", 1, new BigDecimal("79.00"))
                .addItem("USB-C Cable", 2, new BigDecimal("19.99"))
                .shippingTo("789 Tech Lane, Austin, TX 78701")
                .payingWith("Credit Card")
                .withStatus(OrderStatus.CONFIRMED)
                .withNotes("Please handle with care - fragile items")
                .estimatedDelivery(LocalDateTime.now().plusDays(5))
                .build();
    }

    public UserRegistration createBasicUserRegistration() {
        UserRegistration registration = UserRegistration.builder()
                .username("johndoe123")
                .email("john.doe@example.com")
                .password("SecureP@ssw0rd!")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("555-123-4567")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .termsAccepted(true)
                .marketingOptIn(false)
                .build();

        registration.validate();
        return registration;
    }

    public UserRegistration createCorporateUserRegistration() {
        UserRegistration registration = UserRegistration.builder()
                .username("jsmith.corp")
                .email("j.smith@corporation.com")
                .password("C0rp0r@teP@ss!")
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("555-987-6543")
                .address("123 Corporate Blvd, Suite 500")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .country("USA")
                .companyName("Global Enterprises Inc.")
                .jobTitle("Senior Software Engineer")
                .referralCode("CORP2024")
                .termsAccepted(true)
                .marketingOptIn(true)
                .build();

        registration.validate();
        return registration;
    }

    public Query createSimpleUserQuery() {
        return QueryBuilder.select("id", "username", "email", "first_name", "last_name")
                .from("users")
                .where("active", "=", true)
                .orderBy("username", "ASC")
                .limit(50)
                .build();
    }

    public Query createComplexOrderQuery() {
        return QueryBuilder.select(
                        "orders.id",
                        "orders.order_number",
                        "users.username",
                        "users.email",
                        "orders.total_amount",
                        "orders.status",
                        "orders.created_at"
                )
                .from("orders")
                .innerJoin("users", "orders.user_id = users.id")
                .where("orders.created_at", ">=", LocalDate.now().minusMonths(3))
                .whereIn("orders.status", Arrays.asList("CONFIRMED", "PROCESSING", "SHIPPED"))
                .whereBetween("orders.total_amount", new BigDecimal("100.00"), new BigDecimal("10000.00"))
                .orderBy("orders.created_at", "DESC")
                .limit(100)
                .build();
    }

    public Query createAggregationQuery() {
        return QueryBuilder.select(
                        "users.id",
                        "users.username",
                        "COUNT(orders.id) as order_count",
                        "SUM(orders.total_amount) as total_spent"
                )
                .from("users")
                .leftJoin("orders", "users.id = orders.user_id")
                .whereNotNull("orders.id")
                .groupBy("users.id", "users.username")
                .orderBy("total_spent", "DESC")
                .limit(20)
                .build();
    }

    public void demonstrateOrderBuilderStepByStep() {
        OrderBuilder builder = OrderBuilder.newOrder()
                .forUser(1L);

        builder.addItem("Product A", 2, new BigDecimal("29.99"));
        System.out.println("Current total after Product A: " + builder.getCurrentTotal());

        builder.addItem("Product B", 1, new BigDecimal("49.99"));
        System.out.println("Current total after Product B: " + builder.getCurrentTotal());

        if (builder.getCurrentTotal().compareTo(new BigDecimal("100.00")) > 0) {
            builder.withNotes("Qualified for free shipping!");
        }

        OrderDetails order = builder
                .shippingTo("123 Main Street")
                .payingWith("PayPal")
                .build();

        System.out.println("Final order: " + order);
    }

    public void demonstrateInvoiceCalculations() {
        List<InvoiceItem> items = new ArrayList<>();
        items.add(InvoiceItem.builder()
                .description("Item 1")
                .quantity(10)
                .unitPrice(new BigDecimal("100.00"))
                .build());

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-DEMO-001")
                .customerName("Demo Customer")
                .customerEmail("demo@example.com")
                .items(items)
                .taxRate(new BigDecimal("0.10"))
                .discountAmount(new BigDecimal("50.00"))
                .build();

        System.out.println("Subtotal: " + invoice.getSubtotal());
        System.out.println("Discount: " + invoice.getDiscountAmount());
        System.out.println("Tax (10%): " + invoice.getTaxAmount());
        System.out.println("Total: " + invoice.getTotalAmount());
    }
}
