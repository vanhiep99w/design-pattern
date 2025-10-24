package com.designpatterns.showcase.builder;

import com.designpatterns.showcase.builder.domain.OrderDetails;
import com.designpatterns.showcase.builder.manual.OrderBuilder;
import com.designpatterns.showcase.common.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrderBuilderTest {

    @Test
    void shouldBuildValidOrder() {
        OrderDetails order = OrderBuilder.newOrder()
                .forUser(1L)
                .addItem("Laptop", 1, new BigDecimal("999.99"))
                .addItem("Mouse", 2, new BigDecimal("25.00"))
                .shippingTo("123 Main St")
                .payingWith("Credit Card")
                .build();

        assertNotNull(order);
        assertEquals(1L, order.getUserId());
        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("1049.99"), order.getTotalAmount());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void shouldCalculateTotalAmountCorrectly() {
        OrderDetails order = OrderBuilder.newOrder()
                .forUser(1L)
                .addItem("Item A", 3, new BigDecimal("10.00"))
                .addItem("Item B", 2, new BigDecimal("15.50"))
                .build();

        assertEquals(new BigDecimal("61.00"), order.getTotalAmount());
    }

    @Test
    void shouldSupportCustomStatus() {
        OrderDetails order = OrderBuilder.newOrder()
                .forUser(1L)
                .addItem("Item", 1, new BigDecimal("10.00"))
                .withStatus(OrderStatus.CONFIRMED)
                .build();

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void shouldSupportOptionalFields() {
        OrderDetails order = OrderBuilder.newOrder()
                .forUser(1L)
                .addItem("Item", 1, new BigDecimal("10.00"))
                .withNotes("Handle with care")
                .shippingTo("456 Oak Ave")
                .payingWith("PayPal")
                .estimatedDelivery(LocalDateTime.now().plusDays(3))
                .build();

        assertEquals("Handle with care", order.getNotes());
        assertEquals("456 Oak Ave", order.getShippingAddress());
        assertEquals("PayPal", order.getPaymentMethod());
        assertNotNull(order.getEstimatedDelivery());
    }

    @Test
    void shouldThrowExceptionWhenUserIdMissing() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            OrderBuilder.newOrder()
                    .addItem("Item", 1, new BigDecimal("10.00"))
                    .build();
        });

        assertEquals("User ID is required", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNoItems() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            OrderBuilder.newOrder()
                    .forUser(1L)
                    .build();
        });

        assertEquals("Order must have at least one item", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenProductNameEmpty() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            OrderBuilder.newOrder()
                    .forUser(1L)
                    .addItem("", 1, new BigDecimal("10.00"))
                    .build();
        });

        assertEquals("Product name cannot be empty", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenQuantityZero() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            OrderBuilder.newOrder()
                    .forUser(1L)
                    .addItem("Item", 0, new BigDecimal("10.00"))
                    .build();
        });

        assertEquals("Quantity must be positive", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUnitPriceNegative() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            OrderBuilder.newOrder()
                    .forUser(1L)
                    .addItem("Item", 1, new BigDecimal("-10.00"))
                    .build();
        });

        assertEquals("Unit price must be positive", exception.getMessage());
    }

    @Test
    void shouldSupportClearingItems() {
        OrderBuilder builder = OrderBuilder.newOrder()
                .forUser(1L)
                .addItem("Item A", 1, new BigDecimal("10.00"))
                .addItem("Item B", 1, new BigDecimal("20.00"));

        assertEquals(2, builder.getItemCount());

        builder.clear();

        assertEquals(0, builder.getItemCount());
    }

    @Test
    void shouldTrackCurrentTotal() {
        OrderBuilder builder = OrderBuilder.newOrder()
                .forUser(1L)
                .addItem("Item A", 2, new BigDecimal("15.00"));

        assertEquals(new BigDecimal("30.00"), builder.getCurrentTotal());

        builder.addItem("Item B", 1, new BigDecimal("10.00"));

        assertEquals(new BigDecimal("40.00"), builder.getCurrentTotal());
    }

    @Test
    void shouldEnsureOrderDetailsIsImmutable() {
        OrderDetails order = OrderBuilder.newOrder()
                .forUser(1L)
                .addItem("Item", 1, new BigDecimal("10.00"))
                .build();

        assertThrows(UnsupportedOperationException.class, () -> {
            order.getItems().clear();
        });
    }

    @Test
    void shouldSupportMethodChaining() {
        OrderBuilder builder = OrderBuilder.newOrder();

        OrderBuilder result1 = builder.forUser(1L);
        OrderBuilder result2 = result1.addItem("Item", 1, new BigDecimal("10.00"));
        OrderBuilder result3 = result2.withNotes("Test");

        assertSame(builder, result1);
        assertSame(result1, result2);
        assertSame(result2, result3);
    }
}
